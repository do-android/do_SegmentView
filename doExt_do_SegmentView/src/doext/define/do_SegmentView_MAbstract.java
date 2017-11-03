package doext.define;

import core.object.DoUIModule;
import core.object.DoProperty;
import core.object.DoProperty.PropertyDataType;


public abstract class do_SegmentView_MAbstract extends DoUIModule{

	protected do_SegmentView_MAbstract() throws Exception {
		super();
	}
	
	/**
	 * 初始化
	 */
	@Override
	public void onInit() throws Exception{
        super.onInit();
        //注册属性
		this.registProperty(new DoProperty("index", PropertyDataType.String, "0", false));
		this.registProperty(new DoProperty("templates", PropertyDataType.String, "", false));
	}
}