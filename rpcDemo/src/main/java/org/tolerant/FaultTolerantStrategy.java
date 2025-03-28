package org.tolerant;

/**
 * 定义了一个用于处理故障容忍（Fault Tolerance）的策略模式
 */
public interface FaultTolerantStrategy {
    Object handler(FaultContext faultContext) throws Exception;
}
