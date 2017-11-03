package dotest.module.activity;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.doext.module.activity.R;

import core.DoServiceContainer;
import core.object.DoUIModule;
import doext.implement.do_SegmentView_Model;
import doext.implement.do_SegmentView_View;
import dotest.module.frame.debug.DoPage;
import dotest.module.frame.debug.DoService;
/**
 * webview组件测试样例
 */
public class SegmentViewTestActivty extends DoTestActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void initModuleModel() throws Exception {
		this.model = new do_SegmentView_Model();
	}
	
	@Override
	protected void initUIView() throws Exception {
		do_SegmentView_View view = new do_SegmentView_View(this);
        DoPage _doPage = new DoPage();
        ((DoUIModule)this.model).setCurrentUIModuleView(view);
        ((DoUIModule)this.model).setCurrentPage(_doPage);
        view.loadView((DoUIModule)this.model);
        LinearLayout uiview = (LinearLayout)findViewById(R.id.uiview);
        uiview.addView(view);
	}

	@Override
	protected void doTestSyncMethod() {
		Map<String, String> _paras_back = new HashMap<String, String>();
        DoService.syncMethod(this.model, "bindItems", _paras_back);
	}

	@Override
	protected void onEvent() {
		DoService.subscribeEvent(this.model, "indexChanged", new DoService.EventCallBack() {
			@Override
			public void eventCallBack(String _data) {
				DoServiceContainer.getLogEngine().writeDebug("事件回调：" + _data);
			}
		});
	}

}
