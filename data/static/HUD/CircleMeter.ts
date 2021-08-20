import {
    DrawingObject,
    Math2D
} from "./HUD";

/**
 * 圆形表盘
 */
export class CircleMeter extends DrawingObject {
    minValue: number = 0.0
    maxValue: number = 1.0
    hasExceptValue: boolean = false

    actualValues: number[]
    exceptValues: number[]

    hintProgressSelection: number = 0
    hintProgressConverter: (progress: number)=>string = progress => progress.toString()
    hintAutoFontSize: number = 0
    hintAutoFontWeight: number = 0.20

    backgroundMeterPaths: Path2D[]
    foregroundMeterPaths: Path2D[]
    foregroundStyles: any[]
    style = {
        line: {
            cap: "square",
            width: 5,
            spacing: 5,
            filter: null,
        },
        bg: {
            lineAlpha: 0.3,
            lineColor: "#FFFFFF",
            circleStartAngle: Math2D.toRadius(135),
            circleEndAngle: Math2D.toRadius(135) + Math2D.toRadius(270),
        },
        fg: {
            lineAlpha: 1,
            lineColor: "#FFFFFF",
            hintTextStyle: "#FFFFFF",
            hintTextAlpha: 1,
            hintTextFont: "bold {1}px HUD"
        }
    }

    constructor(min: number, max: number, hasExceptValue?: boolean, valueCount?: number) {
        super()
        this.minValue = min
        this.maxValue = max
        this.hasExceptValue = (!hasExceptValue) ? false : hasExceptValue;
        this.foregroundMeterPaths = new Array(valueCount || 1);
        this.backgroundMeterPaths = new Array(valueCount || 1);
        this.actualValues = new Array(valueCount || 1);
        this.exceptValues = new Array(valueCount || 1);
        this.foregroundStyles = new Array(valueCount || 1);
    }

    setProgressColor(index: number, style: string | CanvasGradient | CanvasPattern) {
        let isChanged = (this.foregroundStyles[index] != style);
        this.foregroundStyles[index] = style;
        if (isChanged) {
            this.createFgPaths();
            this.invalidate()
        }
    }

    selectHintProgress(index: number, hintConverter: (progress: number)=>string) {
        this.hintProgressSelection = index
        this.hintProgressConverter = hintConverter
    }

    updateSize(width: number, height: number) {
        super.updateSize(width, height)
        this.createBgPaths();
        this.createFgPaths();
        this.updateHintAutoFontSize();
    }

    updateActualProgress(index: number, progress: number) {
        let isChanged = (this.actualValues[index] != progress);
        this.actualValues[index] = progress;
        if (isChanged) {
            this.createFgPaths();
            this.invalidate()
        }
    }

    updateExceptProgress(index: number, progress: number) {
        this.exceptValues[index] = progress;
        // TODO
    }

    private progressToAngle(progress: number): number {
        const bg = this.style.bg
        const startAngle = bg.circleStartAngle
        const endAngle = bg.circleEndAngle
        const maxAngle = endAngle - startAngle
        const normProgress = (progress - this.minValue) / (this.maxValue - this.minValue);
        return normProgress * maxAngle;
    }

    private createFgPaths() {
        const lineStyle = this.style.line
        const interval = lineStyle.width + lineStyle.spacing
        const startAngle = this.style.bg.circleStartAngle
        for (let i = 0; i < this.foregroundMeterPaths.length; i++) {
            this.foregroundMeterPaths[i] = this.createCirclePath(
                this.clientArea.width(),
                this.clientArea.height(),
                interval * i,
                startAngle, startAngle + this.progressToAngle(this.actualValues[i])
            )
        }
    }

    private createBgPaths() {
        const lineStyle = this.style.line
        const bg = this.style.bg
        const interval = lineStyle.width + lineStyle.spacing
        const startAngle = bg.circleStartAngle
        const endAngle = bg.circleEndAngle
        for (let i = 0; i < this.backgroundMeterPaths.length; i++) {
            this.backgroundMeterPaths[i] = this.createCirclePath(
                this.clientArea.width(),
                this.clientArea.height(),
                interval * i,
                startAngle, endAngle
            )
        }
    }

    private updateHintAutoFontSize() {
        const lineStyle = this.style.line
        const bg = this.style.bg
        const interval = lineStyle.width + lineStyle.spacing
        let minRemindSpacing = Math.min(this.clientArea.width(), this.clientArea.height())
            - interval * Math.max(this.backgroundMeterPaths.length - 1, 0)
        this.hintAutoFontSize = minRemindSpacing * this.hintAutoFontWeight;
    }

    private createCirclePath(width: number, height: number, padding: number, startAngle: number, endAngle: number): Path2D {
        let backgroundRadius = Math.max(((width < height) ? width : height) * 0.5 - padding, 0)
        let path = new Path2D()
        path.arc(
            width * 0.5, height * 0.5,
            backgroundRadius,
            startAngle, endAngle)
        return path
    }

    onDraw(ctx: CanvasRenderingContext2D) {
        this.drawBackground(ctx);
        this.drawForeground(ctx);
        this.drawHintProgress(ctx);
    }

    private drawForeground(ctx: CanvasRenderingContext2D) {
        ctx.save()
        // @ts-ignore
        ctx.lineCap = this.style.line.cap
        ctx.lineWidth = this.style.line.width
        ctx.globalAlpha = this.style.fg.lineAlpha
        ctx.filter = "blur"
        this.foregroundMeterPaths.every((value, index) => {
            ctx.strokeStyle = this.foregroundStyles[index] || this.style.fg.lineColor
            ctx.stroke(this.foregroundMeterPaths[index])
            return true;
        })
        ctx.restore()
    }

    private drawHintProgress(ctx: CanvasRenderingContext2D) {
        const hint = this.hintProgressConverter(this.actualValues[this.hintProgressSelection])
        ctx.save()
        ctx.fillStyle = this.style.fg.hintTextStyle
        ctx.textAlign = "center"
        ctx.font = this.style.fg.hintTextFont.replace("{1}", this.hintAutoFontSize.toString())

        const metrics = ctx.measureText(hint)
        const baseline = (metrics.actualBoundingBoxAscent - metrics.actualBoundingBoxDescent) * 0.5
        ctx.fillText(hint, this.clientArea.width() * 0.5, this.clientArea.height() * 0.5 + baseline)
        ctx.restore()
    }

    private drawBackground(ctx: CanvasRenderingContext2D) {
        ctx.save()
        ctx.strokeStyle = this.style.bg.lineColor
        // @ts-ignore
        ctx.lineCap = this.style.line.cap
        ctx.lineWidth = this.style.line.width
        ctx.globalAlpha = this.style.bg.lineAlpha
        ctx.filter = "blur"
        this.backgroundMeterPaths.every((value, index) => {
            ctx.stroke(this.backgroundMeterPaths[index])
            return true;
        })
        ctx.restore()
    }

}