package com.qaprosoft.carina.core.foundation.utils.android.recorder.utils;

import com.sun.jna.Native;
interface Kernel32 extends W32API {
    Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class, DEFAULT_OPTIONS);
    HANDLE GetCurrentProcess();
    int GetProcessId(HANDLE Process);
}
