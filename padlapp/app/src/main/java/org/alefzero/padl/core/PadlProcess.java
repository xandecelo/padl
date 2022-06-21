package org.alefzero.padl.core;

import org.alefzero.padl.core.model.PadlConfig;
import org.alefzero.padl.targets.PadlTarget;
import org.alefzero.padl.targets.TargetManager;

/**
 * Processing features of this padl instance.
 */
public class PadlProcess {

    PadlConfig config = new PadlConfig();

    public PadlProcess(PadlConfig config) {
        this.config = config;
    }

    public void run() {

        PadlTarget target = TargetManager.getInstance(config.getType());

        // get target config + conn
        // for each source
        //   connect
        //   configphase 
        //     getconfig source
        //     setconfig at target
        //   importphase
        //     getData source
        //     setData target

    }

}
