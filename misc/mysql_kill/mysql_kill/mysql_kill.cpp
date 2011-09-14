// mysql_kill.cpp : a variation on the unix "kill" for use with mysqld on windows
// Copyright (c) 2007-2008 MySQL AB, 2008-2009 Sun Microsystems, Inc. 
// Author: Eric Herman 

// SIGHUP is replaced by OpenEvent(EVENT_MODIFY_STATE, FALSE, "MySQLShutdown<PID>");

#include "stdafx.h"

void Usage(const char *argv0)
{
        char usage[2048];
        sprintf_s(usage, 2048, "USAGE: %s [-0 -9] PID", argv0);
        printf(usage);
}

int SetEventMySQLShutdown(DWORD pid)
{
        char buff[60];
        sprintf_s(buff, 60, "MySQLShutdown%d", pid );
        return OpenAndSetEvent(EVENT_MODIFY_STATE, buff);
}

int main(int argc, const char** argv)
{
        DWORD pid = 0;

        if ((argc == 2) && (sscanf_s(argv[1], "%30u", &pid) == 1))
        {
                return SetEventMySQLShutdown(pid);
        }
        
        if ((argc == 3) && (sscanf_s(argv[2], "%30u", &pid) == 1))
        {
                if (strcmp(argv[1], "-0")==0)
                {
                        return KnownProcess(pid);
                }
                if (strcmp(argv[1], "-1")==0)
                {
                        return SetEventMySQLShutdown(pid);
                }
                if (strcmp(argv[1], "-9")==0)
                {
                        return Terminate(pid);
                }
        }

        Usage(argv[0]);
        return 1;
}
