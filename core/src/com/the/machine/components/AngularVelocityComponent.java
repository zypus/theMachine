package com.the.machine.components;

import com.the.machine.framework.components.AbstractComponent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Created by Frans on 12-3-2015.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AngularVelocityComponent extends AbstractComponent {
    private float angularVelocity;      // In degrees per second
}
