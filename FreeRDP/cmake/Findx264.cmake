
if (X264_INCLUDE_DIR AND X264_LIBRARY)
	set(X264_FIND_QUIETLY TRUE)
endif (X264_INCLUDE_DIR AND X264_LIBRARY)

find_path(X264_INCLUDE_DIR NAMES x264.h
	PATH_SUFFIXES include
	HINTS ${X264_ROOT})
find_library(X264_LIBRARY
	 NAMES x264
	 PATH_SUFFIXES lib
	 HINTS ${X264_ROOT})

include(FindPackageHandleStandardArgs)
FIND_PACKAGE_HANDLE_STANDARD_ARGS(x264 DEFAULT_MSG X264_LIBRARY X264_INCLUDE_DIR)

if (X264_INCLUDE_DIR AND X264_LIBRARY)
	set(X264_FOUND TRUE)
	set(X264_LIBRARIES ${X264_LIBRARY})
endif (X264_INCLUDE_DIR AND X264_LIBRARY)

if (X264_FOUND)
	if (NOT X264_FIND_QUIETLY)
		message(STATUS "Found x264: ${X264_LIBRARIES}")
	endif (NOT X264_FIND_QUIETLY)
else (X264_FOUND)
	if (X264_FIND_REQUIRED)
		message(FATAL_ERROR "x264 was not found")
	endif(X264_FIND_REQUIRED)
endif (X264_FOUND)

mark_as_advanced(X264_INCLUDE_DIR X264_LIBRARY)

