package com.chadate.spellelemental.element.attachment.config;

import com.google.gson.annotations.SerializedName;

/**
 * 最小化的元素附着配置：仅保留 id、法术学派与视觉效果。
 */
public class UnifiedElementAttachmentConfig {
    @SerializedName("element_id")
    private String elementId;

    @SerializedName("school")
    private String school;

    @SerializedName("visual")
    private VisualConfig visual;

    public UnifiedElementAttachmentConfig() {}

    public UnifiedElementAttachmentConfig(String elementId, String school) {
        this.elementId = elementId;
        this.school = school;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public VisualConfig getVisual() {
        return visual;
    }

    public void setVisual(VisualConfig visual) {
        this.visual = visual;
    }

    /**
     * 视觉效果配置（已简化）
     */
    public static class VisualConfig {
        @SerializedName("particle_effect")
        private String particleEffect;
        
        @SerializedName("icon")
        private String icon;
        
        public String getParticleEffect() {
            return particleEffect;
        }
        
        public void setParticleEffect(String particleEffect) {
            this.particleEffect = particleEffect;
        }
        
        public String getIcon() {
            return icon;
        }
        
        public void setIcon(String icon) {
            this.icon = icon;
        }
    }
}
