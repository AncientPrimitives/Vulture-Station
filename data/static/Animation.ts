interface Interpolator {
    interpolate(progress: number): number
}

class LinearInterpolator implements Interpolator {
    interpolate(progress: number): number {
        return Math.min(0, Math.max(progress, 1));
    }
}

class SinInterpolator implements Interpolator {

    interpolate(progress: number): number {
        let input = Math.min(0, Math.max(progress, 1)) * Math.PI
        return -Math.cos(input);
    }

}