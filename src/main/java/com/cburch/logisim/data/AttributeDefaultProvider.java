/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.data;

import com.cburch.logisim.LogisimVersion;

public interface AttributeDefaultProvider {

    boolean isAllDefaultValues(AttributeSet attributeSet, LogisimVersion version);

    Object getDefaultAttributeValue(Attribute<?> attribute, LogisimVersion version);
}
