package com.chadate.spellelemental.client.render;

public final class ElementIconRenderConfig {
	private static final ElementIconRenderConfig INSTANCE = new ElementIconRenderConfig();

	// 可调参数（默认值可按需修改）
	private float quadScale = 0.15f; // 贴图缩放（越小越小）
	private double verticalOffset = 0.6D; // 头顶上方高度
	private double horizontalSpacing = 0.35D; // 多个图标之间的间距
	private float alpha = 0.9f; // 图标透明度
	private float zDepth = 0.1f; // 四边形Z深度（避免Z冲突）
	private boolean faceCamera = true; // 是否始终面向相机
	private boolean flipBillboard = false; // billboard翻转（匹配名牌渲染）

	// 闪烁相关
	private int flashingThreshold = 50; // 小于等于该剩余值开始闪烁与降透明
	private float minAlpha = 0.25f; // 最低透明度
	private float maxAlpha = 0.9f;  // 最高透明度
	private float maxFlashHz = 8.0f; // 剩余为1时的闪烁频率（每秒）
	private float minFlashHz = 2.0f; // 刚达阈值时的闪烁频率（每秒）

	private ElementIconRenderConfig() {}

	public static ElementIconRenderConfig get() { return INSTANCE; }

	public float getQuadScale() { return quadScale; }
	public void setQuadScale(float quadScale) { this.quadScale = quadScale; }
	public double getVerticalOffset() { return verticalOffset; }
	public void setVerticalOffset(double verticalOffset) { this.verticalOffset = verticalOffset; }
	public double getHorizontalSpacing() { return horizontalSpacing; }
	public void setHorizontalSpacing(double horizontalSpacing) { this.horizontalSpacing = horizontalSpacing; }
	public float getAlpha() { return alpha; }
	public void setAlpha(float alpha) { this.alpha = alpha; }
	public float getZDepth() { return zDepth; }
	public void setZDepth(float zDepth) { this.zDepth = zDepth; }
	public boolean isFaceCamera() { return faceCamera; }
	public void setFaceCamera(boolean faceCamera) { this.faceCamera = faceCamera; }
	public boolean isFlipBillboard() { return flipBillboard; }
	public void setFlipBillboard(boolean flipBillboard) { this.flipBillboard = flipBillboard; }

	public int getFlashingThreshold() { return flashingThreshold; }
	public void setFlashingThreshold(int flashingThreshold) { this.flashingThreshold = flashingThreshold; }
	public float getMinAlpha() { return minAlpha; }
	public void setMinAlpha(float minAlpha) { this.minAlpha = minAlpha; }
	public float getMaxAlpha() { return maxAlpha; }
	public void setMaxAlpha(float maxAlpha) { this.maxAlpha = maxAlpha; }
	public float getMaxFlashHz() { return maxFlashHz; }
	public void setMaxFlashHz(float maxFlashHz) { this.maxFlashHz = maxFlashHz; }
	public float getMinFlashHz() { return minFlashHz; }
	public void setMinFlashHz(float minFlashHz) { this.minFlashHz = minFlashHz; }
} 