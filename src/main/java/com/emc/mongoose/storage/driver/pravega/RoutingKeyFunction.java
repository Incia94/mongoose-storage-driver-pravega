package com.emc.mongoose.storage.driver.pravega;

import com.emc.mongoose.base.item.DataItem;
import java.util.function.Function;

public interface RoutingKeyFunction<I extends DataItem>
				extends Function<I, String> {

	long period();
}
