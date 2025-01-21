package interfaces;

import listener.ControllerListener;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import net.java.games.input.Component.Identifier;

public class TController implements Runnable {

    private Controller controller       = null;
    protected boolean U                 = false;
    protected boolean D                 = false;
    protected boolean L                 = false;
    protected boolean R                 = false;
    protected boolean S                 = false;
    protected boolean B                 = false;
    private long FPS                    = 0;
    private ControllerListener listener = null;

    public TController(long FPS, ControllerListener listener) {
        /* Get the available controllers */
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        this.listener = listener;
        for (int i = 0; controllers != null && i < controllers.length; i++) {
            var temp  = controllers[i];
            if (temp.getClass().getName().equals("net.java.games.input.DIAbstractController")) {
                this.controller = temp;
                this.FPS = FPS / 1_000_000;
                break;
            }
        }
    }

    /* 
        Verifica se a variável está inicializada
    */
    public boolean hasAnyConnectedController() {
        return (this.controller != null);
    }

    @Override
    public void run() {
        if (this.controller != null) {
            while (true) {
                if (!controller.poll()) {
                    this.U          = false;
                    this.D          = false;
                    this.L          = false;
                    this.R          = false;
                    this.S          = false;
                    this.B          = false;
                    this.controller = null;
                    break;
                } else {
                    /* Get the controllers event queue */
                    EventQueue queue = controller.getEventQueue();

                    /* Create an event object for the underlying plugin to populate */
                    Event event = new Event();

                    /* For each object in the queue */
                    while (queue.getNextEvent(event)) {
                        
                        Component comp = event.getComponent();
                        Identifier id = comp.getIdentifier();
                        
                        if (null != id && "pov".equals(id.toString())) {
                            if (comp.getPollData() == 0.25f) {
                                this.U = true;
                                this.D = false;
                                this.L = false;
                                this.R = false;
                            } else if (comp.getPollData() == 0.375f) {
                                this.U = true;
                                this.L = true;
                                this.R = false;
                                this.D = false;
                            } else if (comp.getPollData() == 0.5f) {
                                this.L = true;
                                this.R = false;
                                this.U = false;
                                this.D = false;
                            }  else if (comp.getPollData() == 0.625f) {
                                this.L = true;
                                this.D = true;
                                this.U = false;
                                this.R = false;
                            } else if (comp.getPollData() == 0.75f) {
                                this.D = true;
                                this.U = false;
                                this.L = false;
                                this.R = false;
                            } else if (comp.getPollData() == 0.875f) {
                                this.D = true;
                                this.R = true;
                                this.U = false;
                                this.L = false;
                            } else if (comp.getPollData() == 1f) {
                                this.R = true;
                                this.L = false;
                                this.U = false;
                                this.D = false;
                            } else if (comp.getPollData() == 0.125f) {
                                this.R = true;
                                this.U = true;
                                this.L = false;
                                this.D = false;
                            } else if (comp.getPollData() == 0f) {
                                this.U = false;
                                this.D = false;
                                this.L = false;
                                this.R = false;
                            }
                        } else {
                            if ("2".equals(comp.getIdentifier().toString()) || "0".equals(comp.getIdentifier().toString())) {
                                if (event.getValue() == 1.0f) {
                                    this.S = true;
                                } else {
                                    this.S = false;
                                }
                            } else if ("3".equals(comp.getIdentifier().toString()) || "1".equals(comp.getIdentifier().toString())) {
                                if (event.getValue() == 1.0f) {
                                    this.B = true;
                                } else {
                                    this.B = false;
                                }
                            }
                        }
                    }
                }

                this.listener.notify(this.U, this.D, this.L, this.R, this.S, this.B);
            
                /*
                */
                try {
                    Thread.sleep(this.FPS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
