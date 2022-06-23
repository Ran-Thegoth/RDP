/**
 * FreeRDP: A Remote Desktop Protocol Implementation
 * Serial Port Device Service Virtual Channel
 *
 * Copyright 2011 O.S. Systems Software Ltda.
 * Copyright 2011 Eduardo Fiss Beloni <beloni@ossystems.com.br>
 * Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include <assert.h>
#include <errno.h>
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include <winpr/collections.h>
#include <winpr/comm.h>
#include <winpr/crt.h>
#include <winpr/stream.h>
#include <winpr/synch.h>
#include <winpr/thread.h>
#include <winpr/wlog.h>

#include <freerdp/freerdp.h>
#include <freerdp/channels/rdpdr.h>
#include <freerdp/channels/log.h>
#include <freerdp/utils/android.h>
#include <jni.h>

#define TAG CHANNELS_TAG("serial.client")

#define MAX_IRP_THREADS 5


static SERIAL_HELPER *serialHelper;

void setSerialHelper(SERIAL_HELPER *helper) {
    serialHelper = helper;
}
   
typedef struct _SERIAL_DEVICE SERIAL_DEVICE;

struct _SERIAL_DEVICE
{
	DEVICE device;
	SERIAL_DRIVER_ID ServerSerialDriverId;
	wLog* log;
	HANDLE MainThread;
	wMessageQueue* MainIrpQueue;
    volatile BOOL canRead;
	/* one thread per pending IRP and indexed according their CompletionId */
	wListDictionary* IrpThreads;
	UINT32 IrpThreadToBeTerminatedCount;
	CRITICAL_SECTION TerminatingIrpThreadsLock;
	rdpContext* rdpcontext;
};

typedef struct _IRP_THREAD_DATA IRP_THREAD_DATA;

struct _IRP_THREAD_DATA
{
	SERIAL_DEVICE* serial;
	IRP* irp;
};

static UINT32 _GetLastErrorToIoStatus(SERIAL_DEVICE* serial)
{
	switch (GetLastError())
	{
		case ERROR_BAD_DEVICE:
			return STATUS_INVALID_DEVICE_REQUEST;

		case ERROR_CALL_NOT_IMPLEMENTED:
			return STATUS_NOT_IMPLEMENTED;

		case ERROR_CANCELLED:
			return STATUS_CANCELLED;

		case ERROR_INSUFFICIENT_BUFFER:
			return STATUS_BUFFER_TOO_SMALL; /* NB: STATUS_BUFFER_SIZE_TOO_SMALL not defined  */

		case ERROR_INVALID_DEVICE_OBJECT_PARAMETER: /* eg: SerCx2.sys' _purge() */
			return STATUS_INVALID_DEVICE_STATE;

		case ERROR_INVALID_HANDLE:
			return STATUS_INVALID_DEVICE_REQUEST;

		case ERROR_INVALID_PARAMETER:
			return STATUS_INVALID_PARAMETER;

		case ERROR_IO_DEVICE:
			return STATUS_IO_DEVICE_ERROR;

		case ERROR_IO_PENDING:
			return STATUS_PENDING;

		case ERROR_NOT_SUPPORTED:
			return STATUS_NOT_SUPPORTED;

		case ERROR_TIMEOUT:
			return STATUS_TIMEOUT;
			/* no default */
	}

	WLog_Print(serial->log, WLOG_INFO, "unexpected last-error: 0x%08" PRIX32 "", GetLastError());
	return STATUS_UNSUCCESSFUL;
}

static UINT serial_process_irp_create(SERIAL_DEVICE* serial, IRP* irp)
{
	DWORD DesiredAccess;
	DWORD SharedAccess;
	DWORD CreateDisposition;
	UINT32 PathLength;

	if (Stream_GetRemainingLength(irp->input) < 32)
		return ERROR_INVALID_DATA;

	Stream_Read_UINT32(irp->input, DesiredAccess);     /* DesiredAccess (4 bytes) */
	Stream_Seek_UINT64(irp->input);                    /* AllocationSize (8 bytes) */
	Stream_Seek_UINT32(irp->input);                    /* FileAttributes (4 bytes) */
	Stream_Read_UINT32(irp->input, SharedAccess);      /* SharedAccess (4 bytes) */
	Stream_Read_UINT32(irp->input, CreateDisposition); /* CreateDisposition (4 bytes) */
	Stream_Seek_UINT32(irp->input);                    /* CreateOptions (4 bytes) */
	Stream_Read_UINT32(irp->input, PathLength);        /* PathLength (4 bytes) */
	if (!Stream_SafeSeek(irp->input, PathLength)) /* Path (variable) */
		return ERROR_INVALID_DATA;
	irp->FileId = irp->devman->id_sequence++; /* FIXME: why not ((WINPR_COMM*)hComm)->fd? */
	irp->IoStatus = STATUS_SUCCESS;
	WLog_Print(serial->log, WLOG_INFO, "%s (DeviceId: %" PRIu32 ", FileId: %" PRIu32 ") created.",
	           serial->device.name, irp->device->id, irp->FileId);
	serialHelper->notify(serial->device.name,1);
error_handle:
	Stream_Write_UINT32(irp->output, irp->FileId); 
	Stream_Write_UINT8(irp->output, 0);            
	return CHANNEL_RC_OK;
}

static UINT serial_process_irp_close(SERIAL_DEVICE* serial, IRP* irp)
{
	if (Stream_GetRemainingLength(irp->input) < 32)
		return ERROR_INVALID_DATA;

	Stream_Seek(irp->input, 32); /* Padding (32 bytes) */

	WLog_Print(serial->log, WLOG_INFO, "%s (DeviceId: %" PRIu32 ", FileId: %" PRIu32 ") closed.",
	           serial->device.name, irp->device->id, irp->FileId);
	irp->IoStatus = STATUS_SUCCESS;
	serialHelper->notify(serial->device.name,0);
error_handle:
	Stream_Zero(irp->output, 5); /* Padding (5 bytes) */
	return CHANNEL_RC_OK;
}

static UINT serial_process_irp_read(SERIAL_DEVICE* serial, IRP* irp)
{
	UINT32 Length;
	UINT64 Offset;
	BYTE* buffer = NULL;
	DWORD nbRead = 0;

	if (Stream_GetRemainingLength(irp->input) < 32)
		return ERROR_INVALID_DATA;

	Stream_Read_UINT32(irp->input, Length); /* Length (4 bytes) */
	Stream_Read_UINT64(irp->input, Offset); /* Offset (8 bytes) */
	Stream_Seek(irp->input, 20);            /* Padding (20 bytes) */
	buffer = (BYTE*)calloc(Length, sizeof(BYTE));

	if (buffer == NULL)
	{
		irp->IoStatus = STATUS_NO_MEMORY;
		goto error_handle;
	}

	WLog_Print(serial->log, WLOG_INFO, "reading %" PRIu32 " bytes from %s", Length,
	           serial->device.name);

    while(serial->canRead) {
        nbRead = serialHelper->read(serial->device.name,buffer,0,Length);
        if(nbRead> 0) break;
        Sleep(5);
    }
    irp->IoStatus = STATUS_SUCCESS;
	WLog_Print(serial->log, WLOG_INFO, "%" PRIu32 " bytes read from %s", nbRead,
	           serial->device.name);
error_handle:
	Stream_Write_UINT32(irp->output, nbRead); /* Length (4 bytes) */

	if (nbRead > 0)
	{
		if (!Stream_EnsureRemainingCapacity(irp->output, nbRead))
		{
			WLog_ERR(TAG, "Stream_EnsureRemainingCapacity failed!");
			free(buffer);
			return CHANNEL_RC_NO_MEMORY;
		}

		Stream_Write(irp->output, buffer, nbRead); /* ReadData */
	}

	free(buffer);
	return CHANNEL_RC_OK;
}

static UINT serial_process_irp_write(SERIAL_DEVICE* serial, IRP* irp)
{
	UINT32 Length;
	UINT64 Offset;
	void* ptr;
	DWORD nbWritten = 0;

	if (Stream_GetRemainingLength(irp->input) < 32)
		return ERROR_INVALID_DATA;

	Stream_Read_UINT32(irp->input, Length); /* Length (4 bytes) */
	Stream_Read_UINT64(irp->input, Offset); /* Offset (8 bytes) */
	if (!Stream_SafeSeek(irp->input, 20))   /* Padding (20 bytes) */
		return ERROR_INVALID_DATA;

	/* MS-RDPESP 3.2.5.1.5: The Offset field is ignored
	 * assert(Offset == 0);
	 *
	 * Using a serial printer, noticed though this field could be
	 * set.
	 */
	WLog_Print(serial->log, WLOG_INFO, "writing %" PRIu32 " bytes to %s", Length,
	           serial->device.name);

	ptr = Stream_Pointer(irp->input);
	if (!Stream_SafeSeek(irp->input, Length))
		return ERROR_INVALID_DATA;
	/* FIXME: CommWriteFile to be replaced by WriteFile */
    serialHelper->write(serial->device.name,ptr,0,Length);
    nbWritten = Length;
	irp->IoStatus = STATUS_SUCCESS;
	WLog_Print(serial->log, WLOG_INFO, "%" PRIu32 " bytes written to %s", nbWritten,
	           serial->device.name);
	Stream_Write_UINT32(irp->output, nbWritten); /* Length (4 bytes) */
	Stream_Write_UINT8(irp->output, 0);          /* Padding (1 byte) */
	return CHANNEL_RC_OK;
}

/**
 * Function description
 *
 * @return 0 on success, otherwise a Win32 error code
 */
static UINT serial_process_irp_device_control(SERIAL_DEVICE* serial, IRP* irp)
{
	UINT32 IoControlCode;
	UINT32 InputBufferLength;
	BYTE* InputBuffer = NULL;
	UINT32 OutputBufferLength;
	BYTE* OutputBuffer = NULL;
	DWORD BytesReturned = 0;

	if (Stream_GetRemainingLength(irp->input) < 32)
		return ERROR_INVALID_DATA;

	Stream_Read_UINT32(irp->input, OutputBufferLength); /* OutputBufferLength (4 bytes) */
	Stream_Read_UINT32(irp->input, InputBufferLength);  /* InputBufferLength (4 bytes) */
	Stream_Read_UINT32(irp->input, IoControlCode);      /* IoControlCode (4 bytes) */
	Stream_Seek(irp->input, 20);                        /* Padding (20 bytes) */

	if (Stream_GetRemainingLength(irp->input) < InputBufferLength)
		return ERROR_INVALID_DATA;

	OutputBuffer = (BYTE*)calloc(OutputBufferLength, sizeof(BYTE));

	if (OutputBuffer == NULL)
	{
		irp->IoStatus = STATUS_NO_MEMORY;
		goto error_handle;
	}

	InputBuffer = (BYTE*)calloc(InputBufferLength, sizeof(BYTE));

	if (InputBuffer == NULL)
	{
		irp->IoStatus = STATUS_NO_MEMORY;
		goto error_handle;
	}

	Stream_Read(irp->input, InputBuffer, InputBufferLength);

    
    switch(IoControlCode) {
        case 0x1b0044:
            WLog_Print(serial->log, WLOG_INFO,"IOCTL_SERIAL_SET_WAIT_MASK = %x", *((uint32_t *)InputBuffer));
            
            break;
        case 0x1B0048:
            if( serialHelper->available(serial->device.name) >0 ) {
                *((uint32_t *)OutputBuffer) = 0x1;
                WLog_Print(serial->log, WLOG_INFO,"WaitCommEvent =  %d",*((uint32_t *)OutputBuffer));
                BytesReturned = sizeof(uint32_t);
            }
            break;
        default:
            WLog_Print(serial->log, WLOG_INFO,
	           "CommDeviceIoControl: CompletionId=%" PRIu32 ", IoControlCode=[0x%" PRIX32 "] %d, Buf Size %d",
	           irp->CompletionId, IoControlCode, IoControlCode,OutputBufferLength);
            break;
    }
	irp->IoStatus = STATUS_SUCCESS;
error_handle:
	assert(OutputBufferLength == BytesReturned);
	Stream_Write_UINT32(irp->output, BytesReturned); /* OutputBufferLength (4 bytes) */

	if (BytesReturned > 0)
	{
		if (!Stream_EnsureRemainingCapacity(irp->output, BytesReturned))
		{
			WLog_ERR(TAG, "Stream_EnsureRemainingCapacity failed!");
			free(InputBuffer);
			free(OutputBuffer);
			return CHANNEL_RC_NO_MEMORY;
		}

		Stream_Write(irp->output, OutputBuffer, BytesReturned); /* OutputBuffer */
	}

	/* FIXME: Why at least Windows 2008R2 gets lost with this
	 * extra byte and likely on a IOCTL_SERIAL_SET_BAUD_RATE? The
	 * extra byte is well required according MS-RDPEFS
	 * 2.2.1.5.5 */
	/* else */
	/* { */
	/* 	Stream_Write_UINT8(irp->output, 0); /\* Padding (1 byte) *\/ */
	/* } */
	free(InputBuffer);
	free(OutputBuffer);
	return CHANNEL_RC_OK;
}

/**
 * Function description
 *
 * @return 0 on success, otherwise a Win32 error code
 */
static UINT serial_process_irp(SERIAL_DEVICE* serial, IRP* irp)
{
	UINT error = CHANNEL_RC_OK;
/*	WLog_Print(serial->log, WLOG_INFO,
	           "IRP MajorFunction: 0x%08" PRIX32 " MinorFunction: 0x%08" PRIX32 "\n",
	           irp->MajorFunction, irp->MinorFunction);
    */
	switch (irp->MajorFunction)
	{
		case IRP_MJ_CREATE:
			error = serial_process_irp_create(serial, irp);
			break;

		case IRP_MJ_CLOSE:
			error = serial_process_irp_close(serial, irp);
			break;

		case IRP_MJ_READ:
			if ((error = serial_process_irp_read(serial, irp)))
				WLog_ERR(TAG, "serial_process_irp_read failed with error %" PRIu32 "!", error);

			break;

		case IRP_MJ_WRITE:
			error = serial_process_irp_write(serial, irp);
			break;

		case IRP_MJ_DEVICE_CONTROL:
			if ((error = serial_process_irp_device_control(serial, irp)))
				WLog_ERR(TAG, "serial_process_irp_device_control failed with error %" PRIu32 "!",
				         error);

			break;

		default:
			irp->IoStatus = STATUS_NOT_SUPPORTED;
			break;
	}

	return error;
}

static DWORD WINAPI irp_thread_func(LPVOID arg)
{
	IRP_THREAD_DATA* data = (IRP_THREAD_DATA*)arg;
	UINT error;
    WLog_INFO(TAG,"irp_thread_func()");
	/* blocks until the end of the request */
	if ((error = serial_process_irp(data->serial, data->irp)))
	{
		WLog_ERR(TAG, "serial_process_irp failed with error %" PRIu32 "", error);
		goto error_out;
	}

	EnterCriticalSection(&data->serial->TerminatingIrpThreadsLock);
	data->serial->IrpThreadToBeTerminatedCount++;
	error = data->irp->Complete(data->irp);
	LeaveCriticalSection(&data->serial->TerminatingIrpThreadsLock);
error_out:

	if (error && data->serial->rdpcontext)
		setChannelError(data->serial->rdpcontext, error, "irp_thread_func reported an error");

	/* NB: At this point, the server might already being reusing
	 * the CompletionId whereas the thread is not yet
	 * terminated */
	free(data);
	ExitThread(error);
	return error;
}

static void create_irp_thread(SERIAL_DEVICE* serial, IRP* irp)
{
	IRP_THREAD_DATA* data = NULL;
	HANDLE irpThread;
	HANDLE previousIrpThread;
	uintptr_t key;
    WLog_INFO(TAG,"create_irp_thread()");
	EnterCriticalSection(&serial->TerminatingIrpThreadsLock);

	while (serial->IrpThreadToBeTerminatedCount > 0)
	{
		/* Cleaning up termitating and pending irp
		 * threads. See also: irp_thread_func() */
		HANDLE irpThread;
		ULONG_PTR* ids;
		int i, nbIds;
		nbIds = ListDictionary_GetKeys(serial->IrpThreads, &ids);

		for (i = 0; i < nbIds; i++)
		{
			DWORD waitResult;
			ULONG_PTR id = ids[i];
			irpThread = ListDictionary_GetItemValue(serial->IrpThreads, (void*)id);
			waitResult = WaitForSingleObject(irpThread, 0);

			if (waitResult == WAIT_OBJECT_0)
			{
				CloseHandle(irpThread);
				ListDictionary_Remove(serial->IrpThreads, (void*)id);
				serial->IrpThreadToBeTerminatedCount--;
			}
			else if (waitResult != WAIT_TIMEOUT)
			{
				/* unexpected thread state */
				WLog_Print(serial->log, WLOG_WARN,
				           "WaitForSingleObject, got an unexpected result=0x%" PRIX32 "\n",
				           waitResult);
				assert(FALSE);
			}

		}

		if (serial->IrpThreadToBeTerminatedCount > 0)
		{
			WLog_Print(serial->log, WLOG_INFO, "%" PRIu32 " IRP thread(s) not yet terminated",
			           serial->IrpThreadToBeTerminatedCount);
			Sleep(1); /* 1 ms */
		}

		free(ids);
	}

	LeaveCriticalSection(&serial->TerminatingIrpThreadsLock);
	key = irp->CompletionId;
	previousIrpThread = ListDictionary_GetItemValue(serial->IrpThreads, (void*)key);

	if (previousIrpThread)
	{
		WLog_Print(serial->log, WLOG_INFO,
		           "IRP recall: IRP with the CompletionId=%" PRIu32 " not yet completed!",
		           irp->CompletionId);
		assert(FALSE); /* unimplemented */
		irp->Discard(irp);
		return;
	}

	if (ListDictionary_Count(serial->IrpThreads) >= MAX_IRP_THREADS)
	{
		WLog_Print(serial->log, WLOG_WARN,
		           "Number of IRP threads threshold reached: %d, keep on anyway",
		           ListDictionary_Count(serial->IrpThreads));
		assert(FALSE); /* unimplemented */
		               /* TODO: MAX_IRP_THREADS has been thought to avoid a
		                * flooding of pending requests. Use
		                * WaitForMultipleObjects() when available in winpr
		                * for threads.
		                */
	}

	/* error_handle to be used ... */
	data = (IRP_THREAD_DATA*)calloc(1, sizeof(IRP_THREAD_DATA));

	if (data == NULL)
	{
		WLog_Print(serial->log, WLOG_WARN, "Could not allocate a new IRP_THREAD_DATA.");
		goto error_handle;
	}

	data->serial = serial;
	data->irp = irp;
	/* data freed by irp_thread_func */
	irpThread = CreateThread(NULL, 0, irp_thread_func, (void*)data, 0, NULL);

	if (irpThread == INVALID_HANDLE_VALUE)
	{
		WLog_Print(serial->log, WLOG_WARN, "Could not allocate a new IRP thread.");
		goto error_handle;
	}

	key = irp->CompletionId;

	if (!ListDictionary_Add(serial->IrpThreads, (void*)key, irpThread))
	{
		WLog_ERR(TAG, "ListDictionary_Add failed!");
		goto error_handle;
	}

	return;
error_handle:
	irp->IoStatus = STATUS_NO_MEMORY;
	irp->Complete(irp);
	free(data);
}

static void terminate_pending_irp_threads(SERIAL_DEVICE* serial)
{
	ULONG_PTR* ids;
	int i, nbIds;
	nbIds = ListDictionary_GetKeys(serial->IrpThreads, &ids);
	WLog_Print(serial->log, WLOG_INFO, "Terminating %d IRP thread(s)", nbIds);

	for (i = 0; i < nbIds; i++)
	{
		HANDLE irpThread;
		ULONG_PTR id = ids[i];
		irpThread = ListDictionary_GetItemValue(serial->IrpThreads, (void*)id);
		TerminateThread(irpThread, 0);

		if (WaitForSingleObject(irpThread, INFINITE) == WAIT_FAILED)
		{
			WLog_ERR(TAG, "WaitForSingleObject failed!");
			continue;
		}

		CloseHandle(irpThread);
		WLog_Print(serial->log, WLOG_INFO, "IRP thread terminated, CompletionId %p", (void*)id);
	}

	ListDictionary_Clear(serial->IrpThreads);
	free(ids);
}

static DWORD WINAPI serial_thread_func(LPVOID arg)
{
	IRP* irp;
	wMessage message;
	SERIAL_DEVICE* serial = (SERIAL_DEVICE*)arg;
	UINT error = CHANNEL_RC_OK;
    WLog_INFO(TAG,"serial_thread_func() enter");
	while (1)
	{
		if (!MessageQueue_Wait(serial->MainIrpQueue))
		{
			WLog_ERR(TAG, "MessageQueue_Wait failed!");
			error = ERROR_INTERNAL_ERROR;
			break;
		}

		if (!MessageQueue_Peek(serial->MainIrpQueue, &message, TRUE))
		{
			WLog_ERR(TAG, "MessageQueue_Peek failed!");
			error = ERROR_INTERNAL_ERROR;
			break;
		}

		if (message.id == WMQ_QUIT)
		{
			terminate_pending_irp_threads(serial);
			break;
		}

		irp = (IRP*)message.wParam;
    
		if (irp)
			create_irp_thread(serial, irp);
	}
    WLog_INFO(TAG,"serial_thread_func() exit");
	if (error && serial->rdpcontext)
		setChannelError(serial->rdpcontext, error, "serial_thread_func reported an error");

	ExitThread(error);
	return error;
}

/**
 * Function description
 *
 * @return 0 on success, otherwise a Win32 error code
 */
static UINT serial_irp_request(DEVICE* device, IRP* irp)
{
    WLog_INFO(TAG,"serial_irp_request");
	SERIAL_DEVICE* serial = (SERIAL_DEVICE*)device;
	assert(irp != NULL);

	if (irp == NULL)
		return CHANNEL_RC_OK;
	if (!MessageQueue_Post(serial->MainIrpQueue, NULL, 0, (void*)irp, NULL))
	{
		WLog_ERR(TAG, "MessageQueue_Post failed!");
		return ERROR_INTERNAL_ERROR;
	}

	return CHANNEL_RC_OK;
}

/**
 * Function description
 *
 * @return 0 on success, otherwise a Win32 error code
 */
static UINT serial_free(DEVICE* device)
{
	UINT error;
	SERIAL_DEVICE* serial = (SERIAL_DEVICE*)device;
	WLog_Print(serial->log, WLOG_INFO, "freeing");
    serial->canRead = FALSE;
	MessageQueue_PostQuit(serial->MainIrpQueue, 0);

	if (WaitForSingleObject(serial->MainThread, INFINITE) == WAIT_FAILED)
	{
		error = GetLastError();
		WLog_ERR(TAG, "WaitForSingleObject failed with error %" PRIu32 "!", error);
		return error;
	}

	CloseHandle(serial->MainThread);
	Stream_Free(serial->device.data, TRUE);
	MessageQueue_Free(serial->MainIrpQueue);
	ListDictionary_Free(serial->IrpThreads);
	DeleteCriticalSection(&serial->TerminatingIrpThreadsLock);
	free(serial);
	return CHANNEL_RC_OK;
}



#ifdef BUILTIN_CHANNELS
#define DeviceServiceEntry serial_DeviceServiceEntry
#else
#define DeviceServiceEntry FREERDP_API DeviceServiceEntry
#endif

/**
 * Function description
 *
 * @return 0 on success, otherwise a Win32 error code
 */
UINT DeviceServiceEntry(PDEVICE_SERVICE_ENTRY_POINTS pEntryPoints)
{
	char* name;
	RDPDR_SERIAL* device;
	size_t i, len;
	SERIAL_DEVICE* serial;
	UINT error = CHANNEL_RC_OK;
	device = (RDPDR_SERIAL*)pEntryPoints->device;
	name = device->Name;
    if(!serialHelper) return ERROR_INTERNAL_ERROR;
	if ((name && name[0])) {
		wLog* log;
		log = WLog_Get("com.freerdp.channel.serial.client");
		WLog_Print(log, WLOG_INFO, "initializing");
		serial = (SERIAL_DEVICE*)calloc(1, sizeof(SERIAL_DEVICE));
		if (!serial) {
			WLog_ERR(TAG, "calloc failed!");
			return CHANNEL_RC_NO_MEMORY;
		}
		serial->canRead = TRUE;
		serial->log = log;
		serial->device.type = RDPDR_DTYP_SERIAL;
		serial->device.name = name;
		serial->device.IRPRequest = serial_irp_request;
		serial->device.Free = serial_free;
		serial->rdpcontext = pEntryPoints->rdpcontext;
		len = strlen(name);
		serial->device.data = Stream_New(NULL, len + 1);
		if (!serial->device.data) {
			WLog_ERR(TAG, "calloc failed!");
			error = CHANNEL_RC_NO_MEMORY;
			goto error_out;
		}

		for (i = 0; i <= len; i++)
			Stream_Write_UINT8(serial->device.data, name[i] < 0 ? '_' : name[i]);
        serial->ServerSerialDriverId = SerialDriverSerialSys;
		WLog_Print(serial->log, WLOG_INFO, "Server's serial driver: (id: %d)", 
		           serial->ServerSerialDriverId);
		serial->MainIrpQueue = MessageQueue_New(NULL);
		if (!serial->MainIrpQueue)
		{
			WLog_ERR(TAG, "MessageQueue_New failed!");
			error = CHANNEL_RC_NO_MEMORY;
			goto error_out;
		}
		serial->IrpThreads = ListDictionary_New(FALSE);
		if (!serial->IrpThreads)
		{
			WLog_ERR(TAG, "ListDictionary_New failed!");
			error = CHANNEL_RC_NO_MEMORY;
			goto error_out;
		}

		serial->IrpThreadToBeTerminatedCount = 0;
		InitializeCriticalSection(&serial->TerminatingIrpThreadsLock);

		if ((error = pEntryPoints->RegisterDevice(pEntryPoints->devman, (DEVICE*)serial)))
		{
			WLog_ERR(TAG, "EntryPoints->RegisterDevice failed with error %" PRIu32 "!", error);
			goto error_out;
		}

		if (!(serial->MainThread =
		          CreateThread(NULL, 0, serial_thread_func, (void*)serial, 0, NULL)))
		{
			WLog_ERR(TAG, "CreateThread failed!");
			error = ERROR_INTERNAL_ERROR;
			goto error_out;
		}
		WLog_INFO(TAG,"Seial channel %s created",serial->device.name);

	}
	return error;
error_out:
	ListDictionary_Free(serial->IrpThreads);
	MessageQueue_Free(serial->MainIrpQueue);
	Stream_Free(serial->device.data, TRUE);
	free(serial);
	return error;
}