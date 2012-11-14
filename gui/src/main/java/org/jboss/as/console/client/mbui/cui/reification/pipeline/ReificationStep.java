/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.as.console.client.mbui.cui.reification.pipeline;

import org.jboss.as.console.client.mbui.aui.aim.InteractionUnit;
import org.jboss.as.console.client.mbui.cui.Context;
import org.jboss.as.console.client.shared.dispatch.AsyncCommand;

/**
 * @author Harald Pehl
 * @date 11/12/2012
 */
public abstract class ReificationStep implements AsyncCommand<Boolean>
{
    private final String name;
    protected InteractionUnit toplevelUnit;
    protected Context context;

    protected ReificationStep(final String name)
    {
        this.name = name;
    }

    public void init(final InteractionUnit interactionUnit, final Context context)
    {
        this.toplevelUnit = interactionUnit;
        this.context = context;

        assert !toplevelUnit.hasParent() : "Top level units are not expected to have parents";
    }

    public boolean isValid()
    {
        return toplevelUnit != null && context != null;
    }

    public String getName()
    {
        return name;
    }
}
