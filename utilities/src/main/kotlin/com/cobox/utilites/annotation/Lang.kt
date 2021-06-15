package com.cobox.utilites.annotation

/**
 * 标注耗时方法
 */
@MustBeDocumented
annotation class BlockThread

/**
 * 标注并发方法
 */
@MustBeDocumented
annotation class ConcurrentThread

/**
 * 标注仅能在EventThread/MainThread上执行的方法
 */
@MustBeDocumented
annotation class EventThread