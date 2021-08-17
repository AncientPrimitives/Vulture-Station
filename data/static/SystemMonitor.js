"use strict";
var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (Object.prototype.hasOwnProperty.call(b, p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        if (typeof b !== "function" && b !== null)
            throw new TypeError("Class extends value " + String(b) + " is not a constructor or null");
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
var Mem = /** @class */ (function () {
    function Mem() {
        this.totalMem = 0;
        this.availableMem = 0;
        this.freeMem = 0;
        this.totalSwap = 0;
        this.freeSwap = 0;
    }
    return Mem;
}());
var DrawingDriver = /** @class */ (function () {
    function DrawingDriver(canvas) {
        this.hasInvalidate = false;
        this.hasResize = false;
        this.clientWidth = 0;
        this.clientHeight = 0;
        this.canvas = canvas;
        this.ctx = canvas.getContext("2d");
        canvas.addEventListener("onresize", this.resize);
        canvas.addEventListener("oninvalid", this.invalidate);
    }
    DrawingDriver.prototype.drawInner = function () {
        this.hasInvalidate = false;
        if (this.ctx) {
            this.onDraw(this.ctx);
        }
    };
    DrawingDriver.prototype.resizeInner = function () {
        this.hasResize = false;
        if (this.canvas) {
            if ((this.canvas.clientWidth != this.clientWidth) || (this.canvas.clientHeight != this.clientHeight)) {
                this.clientWidth = this.canvas.clientWidth;
                this.clientHeight = this.canvas.clientHeight;
                this.canvas.width = this.clientWidth;
                this.canvas.height = this.clientHeight;
                console.log("[resizeInner] " + this.canvas.width + "x" + this.canvas.height);
                this.onResize(this.clientWidth, this.clientHeight);
            }
        }
    };
    DrawingDriver.prototype.requestResize = function () {
        if (this.hasResize)
            return;
        var instance = this;
        requestAnimationFrame(function () {
            instance.resizeInner();
            instance.drawInner();
        });
    };
    DrawingDriver.prototype.requestRender = function () {
        if (this.hasInvalidate)
            return;
        var instance = this;
        requestAnimationFrame(function () {
            instance.drawInner();
        });
    };
    DrawingDriver.prototype.resize = function (event) {
        this.requestResize();
    };
    DrawingDriver.prototype.invalidate = function (event) {
        this.requestRender();
    };
    DrawingDriver.prototype.onResize = function (width, height) { };
    DrawingDriver.prototype.onDraw = function (ctx) { };
    return DrawingDriver;
}());
var MemSystemMonitor = /** @class */ (function (_super) {
    __extends(MemSystemMonitor, _super);
    function MemSystemMonitor(canvas) {
        var _this = _super.call(this, canvas) || this;
        _this.mem = new Mem();
        _this.memMeter = new CircleMeter(0.0, 1.0, false, 1);
        _this.swapMeter = new CircleMeter(0.0, 1.0, false, 1);
        _this.createMemMeter();
        return _this;
    }
    MemSystemMonitor.prototype.createMemMeter = function () {
        this.memMeter.attachToRoot(this);
        this.swapMeter.attachToRoot(this);
        this.memMeter.hintAutoFontWeight = 0.25;
        this.swapMeter.hintAutoFontWeight = 0.25;
        this.memMeter.selectHintProgress(0, this.convertProgress);
        this.swapMeter.selectHintProgress(0, this.convertProgress);
    };
    MemSystemMonitor.prototype.convertProgress = function (progress) {
        return (progress * 100).toFixed(1).toString() + "%";
    };
    MemSystemMonitor.prototype.updateMemInfo = function (totalMem, availableMem, freeMem, totalSwap, freeSwap) {
        this.mem.totalMem = totalMem;
        this.mem.availableMem = availableMem;
        this.mem.freeMem = freeMem;
        this.mem.totalSwap = totalSwap;
        this.mem.freeSwap = freeSwap;
        this.memMeter.updateActualProgress(0, (totalMem - availableMem) / totalMem);
        this.swapMeter.updateActualProgress(0, (totalSwap - freeSwap) / totalSwap);
        this.requestResize();
    };
    MemSystemMonitor.prototype.onResize = function (width, height) {
        _super.prototype.onResize.call(this, width, height);
        var size = Math.min(width, height) * 0.65;
        var spacing = (height - size) * 0.5;
        var swpX = width - size - spacing;
        var swpY = (height - size) * 0.5;
        var memX = swpX - size - spacing;
        var memY = swpY;
        this.memMeter.setPosition(memX, memY);
        this.memMeter.updateSize(size, size);
        this.swapMeter.setPosition(swpX, swpY);
        this.swapMeter.updateSize(size, size);
    };
    MemSystemMonitor.prototype.onDraw = function (ctx) {
        _super.prototype.onDraw.call(this, ctx);
        ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.memMeter.draw(ctx);
        this.swapMeter.draw(ctx);
    };
    return MemSystemMonitor;
}(DrawingDriver));
