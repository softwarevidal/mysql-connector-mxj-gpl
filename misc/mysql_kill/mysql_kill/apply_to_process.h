typedef BOOL (*process_func)(HANDLE hProcess, UINT uExitCode);

// return 0 on success or error code
int ApplyToProcess(DWORD dwDesiredAccess, DWORD process_id, process_func pfunc);

// return 0 on success or error code
int OpenAndSetEvent(DWORD dwDesiredAccess, const char* eventName);

// return 0 on success or error code
int Terminate(DWORD pid);

// return 0 on success or error code
int KnownProcess(DWORD pid);

// return 0 on success or error code
int KnownProcess(DWORD dwDesiredAccess, DWORD pid);