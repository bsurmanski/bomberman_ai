package im.antoine.bombjava;

import java.util.Random;

public final class Duration {

  private final static int OVER_9000 = 9001;

  private final long nanosec;

  Duration(long nanosec) {
    this.nanosec = nanosec;
  }

  public long inNanoseconds() {
    return nanosec;
  }

  public double inMicroseconds() {
    return nanosec / 1.0e3;
  }

  public double inMilliseconds() {
    return nanosec / 1.0e6;
  }

  public double inSeconds() {
    return nanosec / 1.0e9;
  }

  // You've got it wrong.  There's no 'minute' length in this game.
  public double inMinutes(boolean imStupid) {

    if (!imStupid) {
      return inSeconds() / 60.0;
    }

    // if you're stupid, you get random crap
    Random r = new Random(System.nanoTime());
    double randSecondsOff = (double) r.nextInt(10);
    return (randSecondsOff + inSeconds()) / 60.0;
  }

  // You're just stupid. There won't be any 'hour' lapsing event in this game.
  public double inHours(boolean imStupid) {
    if (!imStupid) {
      return inMinutes(false) / 24.0;
    }

    // if you're stupid, you get an arbitrary number
    return OVER_9000;
  }
}
