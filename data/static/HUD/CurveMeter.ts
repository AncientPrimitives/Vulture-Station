import {
    DrawingObject
} from "./HUD";

export interface SlidingWindowAdapter<T> {
    getDataCount(): T
    getVisibleRange(): [min: T, max: T]
    getData(position: number): T
}

export class CurveMeter extends DrawingObject {

    style = {
        bg: {
            grid: {
                lineColor: "#FFFFFF",
                lineAlpha: 0.1,
                lineWidth: 1,
                xRangeGrids: 4,
                yRangeGrids: 4
            }
        }
    };

    gridPath: Path2D | null = null;
    gridChangeMask: string = ""

    onDraw(ctx: CanvasRenderingContext2D) {
        super.onDraw(ctx);
        this.drawGrid(ctx);
        this.drawCurve(ctx);
    }

    private calcGridChangeMask(): string {
        return "" + this.style.bg.grid.xRangeGrids + this.style.bg.grid.yRangeGrids
                  + this.clientArea.width() + this.clientArea.height()
    }

    private buildGridPath() {
        let lineWidthOffset = Math.ceil(this.style.bg.grid.lineWidth * 0.5)
        let left = this.clientArea.left + lineWidthOffset
        let right = this.clientArea.right - lineWidthOffset
        let top = this.clientArea.top + lineWidthOffset
        let bottom = this.clientArea.bottom - lineWidthOffset

        // frame
        let grid = new Path2D()
        grid.rect(left, top, right - left, bottom - top)

        // inner lines
        let lineLeft = this.clientArea.left + this.style.bg.grid.lineWidth
        let lineRight = this.clientArea.right - this.style.bg.grid.lineWidth
        let lineTop = this.clientArea.top + this.style.bg.grid.lineWidth
        let lineBottom = this.clientArea.bottom - this.style.bg.grid.lineWidth
        let verticalStep = (bottom - top) / Math.max(1, this.style.bg.grid.xRangeGrids)
        let horizontalStep = (right - left) / Math.max(1, this.style.bg.grid.yRangeGrids)
        for (let i = 1; i < this.style.bg.grid.yRangeGrids; i++) { // horizon
            grid.moveTo(lineLeft, lineTop + horizontalStep * i);
            grid.lineTo(lineRight, lineTop + horizontalStep * i)
        }
        for (let i = 1; i < this.style.bg.grid.xRangeGrids; i++) { // vertical
            grid.moveTo(lineLeft + verticalStep * i, lineTop);
            grid.lineTo(lineLeft + verticalStep * i, lineBottom);
        }
    }

    private drawGrid(ctx: CanvasRenderingContext2D) {
        const currentChangeMask = this.calcGridChangeMask()
        if (currentChangeMask != this.gridChangeMask) {
            this.gridChangeMask = currentChangeMask;
            this.buildGridPath()
        }
        if (this.gridPath) {
            ctx.strokeStyle = this.style.bg.grid.lineColor;
            ctx.globalAlpha = this.style.bg.grid.lineAlpha;
            ctx.lineWidth = this.style.bg.grid.lineWidth;
            ctx.lineCap = "butt";
            ctx.stroke(this.gridPath)
        }
    }

    private drawCurve(ctx: CanvasRenderingContext2D) {

    }

    protected getStartBound(): number {
        return 0
    }

    protected getEndBound(): number {
        return 0
    }

    protected getTopBound(): number {
        return 0
    }

    protected getBottomBound(): number {
        return 0
    }
}

export class ScrollableCurveMeter extends CurveMeter {
    constructor() {
        super();
    }
}