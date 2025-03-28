package org.tolerant;

/**
 * 快速失败（Fail Fast）是一种容错策略，其核心思想是：
 * 当系统遇到错误或异常时，立即停止处理，而不是尝试修复或继续执行。
 * 这种策略的优点是能够快速暴露问题，避免错误扩散或隐藏，从而便于调试和修复。
 * 在这个实现中，handler 方法直接返回了异常对象，而不是尝试处理或恢复，这符合快速失败策略的核心思想。
 */
public class FailFastFaultTolerantStrategy implements FaultTolerantStrategy{
    @Override
    public Object handler(FaultContext faultContext) throws Exception {
        return faultContext.getException();
    }
}
