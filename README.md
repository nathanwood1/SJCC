# SJCC
Simple Java Canvas Controls
## What is it?
SJCC is a simple one-class library for creating a render method, along with all you'll need for inputs:  
- Keyboard
- Mouse
- Mousewheel

## Why use it?
It is _**really, really**_ lightweight.  
I don't mind if you don't give credit for using this.
## How do you use it?
```java
//Excluding imports for simplicity
public class TestWindow extends SJCC {
  
  public static void main(String[] args) {
    TestWindow t = new TestWindow();
    t.WIDTH = 800; //Example
    t.HEIGHT = 600;
    t.TITLE = "Test Window";
    t.start();
  }

  @Override
  public void render(Graphics2D g, double delta) {
    //Put anything in here you want
    if (getMouseWheel() != 0) {
      System.out.println("Mouse wheel moved!");
    }
    if (getKey(KeyEvent.VK_SPACE) == 0) {
      System.out.println("Space just pressed!");
    }
    if (mouseDown()) {
      if (mouseKey() == MouseEvent.BUTTON1) {
        System.out.println("Left mouse button pressed at" + 
        clickMousePos().x + ", " + clickMousePos().y);
      }
    }
  }

}
```
#### Things to note with mouse
clickMousePos() returns the position of a mouse click, whilst currentMousePos() returns the current position of a mouse click.
All mouse positions are realative to the canvas.
getKey() returns a double representing the time it has been held down for.
## Download
_**No** current releases._ **Please wait**
