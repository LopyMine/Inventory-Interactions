package net.lopymine.ipi.config.base;

public record ItemOffset(double x, double y, double width, double height) {

	public double getOffsetX() {
		return this.x() - this.getDimensionOffsetX();
	}

	public double getOffsetY() {
		return this.y() - this.getDimensionOffsetY();
	}

	public double getDimensionOffsetX() {
		return (this.width - 16D) / 2D;
	}

	public double getDimensionOffsetY() {
		return (this.height - 16D) / 2D;
	}

}
