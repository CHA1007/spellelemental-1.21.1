package com.chadate.spellelemental.util;

/**
 * 统一的“不可附着伤害”线程上下文标记。
 * 用于在执行某些伤害（例如元素反应的范围/直接伤害）时，
 * 避免再次触发“受伤即附着”的逻辑，造成连锁附着。
 */
public final class DamageAttachmentGuards {
    private DamageAttachmentGuards() {}

    // 使用可嵌套计数而非简单布尔，保证嵌套调用时状态正确恢复
    private static final ThreadLocal<Integer> NON_ATTACHABLE_NEST = ThreadLocal.withInitial(() -> 0);

    /**
     * 在本线程内将伤害标记为“不可附着”执行给定逻辑，结束后自动恢复。
     */
    public static void runAsNonAttachable(Runnable action) {
        int prev = NON_ATTACHABLE_NEST.get();
        NON_ATTACHABLE_NEST.set(prev + 1);
        try {
            action.run();
        } finally {
            NON_ATTACHABLE_NEST.set(prev);
        }
    }

    /**
     * 当前线程是否处于“不可附着伤害”上下文中。
     */
    public static boolean isNonAttachable() {
        return NON_ATTACHABLE_NEST.get() > 0;
    }
}
