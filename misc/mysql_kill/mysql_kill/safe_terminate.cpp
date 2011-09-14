// safe_terminate.cpp : Safe version of TerminateProcess(HANDLE hProcess, UINT uExitCode)
// Copyright (c) 2007-2008 MySQL AB, 2008-2009 Sun Microsystems, Inc. 
// Author: (Adaptded by code written by Reggie Burnett) Eric Herman

#include "stdafx.h"

BOOL SafeTerminateProcess(HANDLE hProcess, UINT uExitCode)
{
        DWORD dwTID, dwCode, dwErr = 0;
        HANDLE hProcessDup = INVALID_HANDLE_VALUE;
        HANDLE hRT = NULL;
        HINSTANCE hKernel = GetModuleHandle("Kernel32");
        BOOL bSuccess = FALSE;

        // warning C4312: 'type cast' : 
        //                conversion from 'UINT' to 'PVOID' of greater size
        // FIXME: Since our usage is always ZERO maybe we don't care, 
        // but still it would be nice to have a clean compile that deals
        // with this warning the "right" way.
        PVOID pvExitCode = (PVOID)uExitCode; 

        BOOL bDup = DuplicateHandle(GetCurrentProcess(),
                                    hProcess, 
                                    GetCurrentProcess(), 
                                    &hProcessDup, 
                                    PROCESS_ALL_ACCESS, 
                                    FALSE, 
                                    0);
 
        // Detect the special case where the process is
        // already dead...
        if ( GetExitCodeProcess((bDup) ? hProcessDup : hProcess, &dwCode)
             && (dwCode == STILL_ACTIVE) )
        {
                FARPROC pfnExitProc;
 
                pfnExitProc = GetProcAddress(hKernel, "ExitProcess");
 
                hRT = CreateRemoteThread((bDup) ? hProcessDup : hProcess, 
                                          NULL,
                                          0,
                                          (LPTHREAD_START_ROUTINE)pfnExitProc, 
                                          pvExitCode,
                                          0,
                                          &dwTID);
 
                if ( hRT == NULL )
                {
                        dwErr = GetLastError();
                }
        }
        else
        {
                dwErr = ERROR_PROCESS_ABORTED;
        }
 
        if ( hRT )
        {
                // Must wait (miliseconds) for process to terminate to
                // guarantee that it has exited...
                WaitForSingleObject((bDup) ? hProcessDup : hProcess, 20 * 1000);
                // WaitForSingleObject((bDup) ? hProcessDup : hProcess, INFINITE);
 
                CloseHandle(hRT);
                bSuccess = TRUE;
        }
 
        if ( bDup )
        {
                CloseHandle(hProcessDup);
        }

        if ( !bSuccess )
        {
                SetLastError(dwErr);
        }
        return bSuccess;
}
