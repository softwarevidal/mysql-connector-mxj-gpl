// apply_to_process.cpp : defines pid functions.
// Copyright (c) 2007-2008 MySQL AB, 2008-2009 Sun Microsystems, Inc. 
// Author: Eric Herman 
// adapted from original "kill.cpp" by Eric Herman and Kendrick Shaw.

#include "stdafx.h"

// return 0 on success or error code
int ApplyToProcess(DWORD dwDesiredAccess, DWORD process_id, process_func pfunc)
{
        UINT uExitCode = 0;

        HANDLE h_process = OpenProcess(dwDesiredAccess, FALSE, process_id);

	if (h_process == NULL)
        {
		return GetLastError();
        }

	BOOL return_val= pfunc(h_process, uExitCode);

	CloseHandle(h_process);
	
        return return_val ? (int) uExitCode : GetLastError();
}

// return 0 on success or error code
int OpenAndSetEvent(DWORD dwDesiredAccess, const char* eventName)
{
        HANDLE hEvent= OpenEvent(dwDesiredAccess, FALSE, eventName);
        if (hEvent == NULL)
        {
                fprintf(stderr, "No HANDLE %s", eventName);
                // fprintf(stderr, "\nLast Error: %d", GetLastError());
		return GetLastError();
        }
        SetEvent(hEvent);
        CloseHandle(hEvent);
        return 0;
}

// return 0 on success or error code
int Terminate(DWORD pid)
{
        // fprint(stderr, "SafeTerminateProcess %d", pid);
        int safe = ApplyToProcess(PROCESS_TERMINATE, pid, &SafeTerminateProcess);
        if( safe == 0 )
        {
                return 0;
        }
        fprintf(stderr, "TerminateProcess %d", pid);
        return ApplyToProcess(PROCESS_TERMINATE, pid, (process_func)&TerminateProcess);
}

BOOL KnownProcessInner(HANDLE h_process, UINT uExitCode)
{
	// already returned if process doesn't exist
	return TRUE;
}

// return 0 on success or error code
int KnownProcess(DWORD dwDesiredAccess, DWORD pid)
{
        return ApplyToProcess(dwDesiredAccess, pid, &KnownProcessInner);
}

// return 0 on success or error code
int KnownProcess(DWORD pid)
{
        return KnownProcess(PROCESS_TERMINATE, pid);
}
