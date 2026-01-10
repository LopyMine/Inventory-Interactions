package net.lopymine.ipi.config.base;

public record ItemOffset(int x, int y, int width, int height) {

	public int getOffsetX() {
		return this.x() - this.getDimensionOffsetX();
	}

	public int getOffsetY() {
		return this.y() - this.getDimensionOffsetY();
	}

	public int getDimensionOffsetX() {
		return (this.width - 16) / 2;
	}

	public int getDimensionOffsetY() {
		return (this.height - 16) / 2;
	}

}
