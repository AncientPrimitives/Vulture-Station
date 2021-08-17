class Rect {
    left: number;
    top: number;
    right: number;
    bottom: number;

    constructor(left: number, top: number, right: number, bottom: number) {
        this.left = left
        this.top = top
        this.right = right
        this.bottom = bottom
    }

    x(): number { return this.left }
    y(): number { return this.top }
    width(): number { return (this.right - this.left) }
    height(): number { return (this.bottom - this.top) }
    centerX(): number { return (this.left + this.width() * 0.5) }
    centerY(): number { return (this.top + this.height() * 0.5) }
}

class Math2D {
    static toDegree(radius: number): number {
        return 180.0 * radius / Math.PI
    }

    static toRadius(degrees: number): number {
        return degrees * Math.PI / 180
    }
}

class DrawingObject {
    static AUTO_SIZE: number = -1

    clientArea: Rect = new Rect(0, 0, DrawingObject.AUTO_SIZE, DrawingObject.AUTO_SIZE);
    root: DrawingDriver | null = null

    attachToRoot(root: DrawingDriver) {
        this.root = root
    }

    setPosition(x: number, y: number) {
        const offsetX = x - this.clientArea.left;
        const offsetY = y - this.clientArea.top;
        this.clientArea.left += offsetX
        this.clientArea.right += offsetX
        this.clientArea.top += offsetY
        this.clientArea.bottom += offsetY
    }

    updateSize(width: number, height: number) {
        this.clientArea.right = this.clientArea.left + width
        this.clientArea.bottom = this.clientArea.top + height
    }

    draw(ctx: CanvasRenderingContext2D) {
        ctx.save()
        ctx.translate(this.clientArea.left, this.clientArea.top)
        this.onDraw(ctx)
        ctx.restore()
    }

    onDraw(ctx: CanvasRenderingContext2D) { }

    invalidate() {
        if (this.root) {
            this.root.requestRender()
        }
    }
}

/**
 * 圆形表盘
 */
class CircleMeter extends DrawingObject {
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
            cap: "round",
            width: 8,
            spacing: 5,
            filter: null,
        },
        bg: {
            lineAlpha: 0.5,
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

// class Label extends DrawingObject {
//
//     style = {
//         contentStyle: "#FFFFFF",
//         contentAlpha: 1,
//         contentFont: "bold {1} HUD",
//         contentFontSize: 0.8,
//         contentFontUnit: "em",
//
//         hintSize: 1, // rect: 边长, circle: 直径
//         hintMarginEnd: 1
//     }
//
//     content: string
//     hintColor: string | CanvasGradient | CanvasPattern | null = null
//     contentFontSize: number = this.style.contentFontSize
//     contentFontUnit: string = this.style.contentFontUnit
//
//     hasMeasuredClientArea: boolean = false
//     hintPath: Path2D | null = null
//
//     constructor(content: string, hintColor?: string) {
//         super();
//         this.content = content
//         this.hintColor = hintColor || ""
//     }
//
//     setFontSize(fontSize: number, fontUnit: string = "em") {
//         this.contentFontSize = fontSize
//         this.contentFontUnit = fontUnit
//         this.hasMeasuredClientArea = false
//     }
//
//     setFontStyle(fontFillStyle: string) {
//         this.style.contentStyle = fontFillStyle
//         this.hasMeasuredClientArea = false
//     }
//
//     draw(ctx: CanvasRenderingContext2D) {
//         super.draw(ctx);
//         this.measureClientArea(ctx);
//
//         ctx.save()
//         ctx.font = this.style.contentFont.replace("{1}", (this.style.contentFontSize + this.style.contentFontUnit))
//         const metrics = ctx.measureText(this.content)
//
//         if (this.hintColor) {
//             ctx.translate()
//         }
//
//         ctx.restore()
//     }
//
//     protected measureClientArea(ctx: CanvasRenderingContext2D) {
//         if (this.hasMeasuredClientArea) return
//
//         ctx.font = this.style.contentFont.replace("{1}", (this.style.contentFontSize + this.style.contentFontUnit))
//         const metrics = ctx.measureText(this.content)
//
//         const labelWidth = metrics.width
//         const labelHeight = metrics.actualBoundingBoxDescent - metrics.actualBoundingBoxAscent
//         const hintWidth = (this.hintColor != null) ? (this.style.hintSize + this.style.hintMarginEnd) : 0
//         const hintHeight = (this.hintColor != null) ? this.style.hintSize : 0
//         this.updateSize(
//             labelWidth + hintWidth,
//             Math.max(labelHeight, hintHeight)
//         )
//
//         this.createHintPath()
//         this.hasMeasuredClientArea = true
//     }
//
//     protected createHintPath() {
//         this.hintPath = new Path2D()
//         const radius = this.style.hintSize
//         this.hintPath.ellipse(radius * 0.5, radius * 0.5, radius, radius, 0, 0, Math.PI * 2)
//         this.hintPath.closePath()
//     }
// }
//
// /**
//  * 带标题的标签
//  */
// class TitledLabel extends Label {
//
//     style = {
//         titleStyle: "#FFFFFF",
//         titleAlpha: 1,
//         titleFont: "bold {1}px HUD",
//         contentStyle: "#FFFFFF",
//         contentAlpha: 1,
//         contentFont: "bold {1}px HUD"
//     }
//
//     title: string
//     content: string
//     hintColor: string = ""
//
//     constructor(title: string, content: string, hintColor?: string) {
//         super();
//         this.title = title
//         this.content = content
//         this.hintColor = hintColor || ""
//     }
//
//     setPosition(x: number, y: number) {
//         super.setPosition(x, y);
//     }
//
//     setFontSize()
//
//     updateSize(width: number, height: number) {
//         super.updateSize(width, height);
//     }
//
//     draw(ctx: CanvasRenderingContext2D) {
//         super.draw(ctx);
//     }
//
// }