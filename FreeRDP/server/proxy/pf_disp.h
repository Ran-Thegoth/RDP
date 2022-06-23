/**
 * FreeRDP: A Remote Desktop Protocol Implementation
 * FreeRDP Proxy Server
 *
 * Copyright 2019 Kobi Mizrachi <kmizrachi18@gmail.com>
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

#ifndef FREERDP_SERVER_PROXY_RDPEDISP_H
#define FREERDP_SERVER_PROXY_RDPEDISP_H

#include <freerdp/client/disp.h>
#include <freerdp/server/disp.h>

#include "pf_context.h"

BOOL pf_server_disp_init(pServerContext* ps);
void pf_disp_register_callbacks(DispClientContext* client, DispServerContext* server,
                                proxyData* pdata);

#endif /*FREERDP_SERVER_PROXY_RDPEDISP_H*/
