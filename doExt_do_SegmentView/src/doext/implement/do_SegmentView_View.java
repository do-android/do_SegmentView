package doext.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import core.DoServiceContainer;
import core.helper.DoJsonHelper;
import core.helper.DoScriptEngineHelper;
import core.helper.DoTextHelper;
import core.helper.DoUIModuleHelper;
import core.interfaces.DoIListData;
import core.interfaces.DoIScriptEngine;
import core.interfaces.DoIUIModuleView;
import core.object.DoInvokeResult;
import core.object.DoMultitonModule;
import core.object.DoUIModule;
import doext.define.do_SegmentView_IMethod;
import doext.define.do_SegmentView_MAbstract;

/**
 * 自定义扩展UIView组件实现类，此类必须继承相应VIEW类，并实现DoIUIModuleView,do_SegmentView_IMethod接口；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.model.getUniqueKey());
 */
@SuppressLint({ "UseSparseArrays", "ClickableViewAccessibility" })
public class do_SegmentView_View extends HorizontalScrollView implements DoIUIModuleView, do_SegmentView_IMethod {

	/**
	 * 每个UIview都会引用一个具体的model实例；
	 */
	private do_SegmentView_MAbstract model;
	private Context mContext;
	private DoIListData mData;
	private List<String> itemTemplatePaths = new ArrayList<String>();
	private List<Integer> itemsX = new ArrayList<Integer>();

	private int itemX;

	public do_SegmentView_View(Context context) {
		super(context);
		this.mContext = context;
		setFillViewport(true);
		setHorizontalScrollBarEnabled(false);
	}

	/**
	 * 初始化加载view准备,_doUIModule是对应当前UIView的model实例
	 */
	@Override
	public void loadView(DoUIModule _doUIModule) throws Exception {
		this.model = (do_SegmentView_MAbstract) _doUIModule;
	}

	/**
	 * 动态修改属性值时会被调用，方法返回值为true表示赋值有效，并执行onPropertiesChanged，否则不进行赋值；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public boolean onPropertiesChanging(Map<String, String> _changedValues) {
		return true;
	}

	/**
	 * 属性赋值成功后被调用，可以根据组件定义相关属性值修改UIView可视化操作；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public void onPropertiesChanged(Map<String, String> _changedValues) {
		DoUIModuleHelper.handleBasicViewProperChanged(this.model, _changedValues);
		if (_changedValues.containsKey("templates")) {
			initViewTemplate(_changedValues.get("templates"));
		}
		if (_changedValues.containsKey("index")) {
			setIndex(DoTextHelper.strToInt(_changedValues.get("index"), 0));
		}
	}

	private void initViewTemplate(String templates) {
		try {
			String[] templateArray = templates.split(",");
			if (templateArray.length == 0) {
				throw new RuntimeException("模版templates.length = 0");
			}
			for (String templatePath : templateArray) {
				itemTemplatePaths.add(templatePath);
			}
		} catch (Exception e) {
			DoServiceContainer.getLogEngine().writeError("解析templates错误： \t", e);
			e.printStackTrace();
		}
	}

	private void setIndex(final int mIndex) {
		this.post(new Runnable() {
			@Override
			public void run() {
				smoothScrollTo(getX(mIndex), 0);
				fireIndexChanged(mIndex);
			}
		});
	}

	private void clickItem(final int mIndex) {
		int index = 0;
		try {
			index = DoTextHelper.strToInt(model.getPropertyValue("index"), 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final int currentIndex = index;
		this.post(new Runnable() {
			@Override
			public void run() {
				smoothScrollTo(getX(mIndex), 0);
				if (currentIndex == mIndex) {
					return;
				}
				model.setPropertyValue("index", mIndex + "");
				fireIndexChanged(mIndex);
			}
		});
	}

	private void fireIndexChanged(int index) {
		DoInvokeResult jsonResult = new DoInvokeResult(model.getUniqueKey());
		jsonResult.setResultInteger(index);
		model.getEventCenter().fireEvent("indexChanged", jsonResult);
	}

	private int getX(int index) {
		int size = itemsX.size();
		if (size == 0) {
			return 0;
		}
		if (index >= size) {
			index = size - 1;
		}
		try {
			return itemsX.get(index);
		} catch (Exception e) {
		}
		return 0;
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("bindItems".equals(_methodName)) {
			bindItems(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("refreshItems".equals(_methodName)) {
			refreshItems(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return false;
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.model.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) {
		//...do something
		return false;
	}

	/**
	 * 释放资源处理，前端JS脚本调用closePage或执行removeui时会被调用；
	 */
	@Override
	public void onDispose() {
		//...do something
	}

	/**
	 * 重绘组件，构造组件时由系统框架自动调用；
	 * 或者由前端JS脚本调用组件onRedraw方法时被调用（注：通常是需要动态改变组件（X、Y、Width、Height）属性时手动调用）
	 */
	@Override
	public void onRedraw() {
		this.setLayoutParams(DoUIModuleHelper.getLayoutParams(this.model));
	}

	/**
	 * 获取当前model实例
	 */
	@Override
	public DoUIModule getModel() {
		return model;
	}

	/**
	 * 绑定item的数据；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void bindItems(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String _address = DoJsonHelper.getString(_dictParas, "data", "");
		if (_address == null || _address.length() <= 0)
			throw new Exception("doSlideView 未指定 data参数！");
		DoMultitonModule _multitonModule = DoScriptEngineHelper.parseMultitonModule(_scriptEngine, _address);
		if (_multitonModule == null)
			throw new Exception("doSlideView data参数无效！");
		if (_multitonModule instanceof DoIListData) {
			mData = (DoIListData) _multitonModule;
			try {
				this.removeAllViews();
				this.addView(getItemView());
			} catch (Exception e) {
				DoServiceContainer.getLogEngine().writeError("bindItems错误： \t", e);
				e.printStackTrace();
			}
		}
	}

	private View getItemView() throws Exception {
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		if (mData != null && itemTemplatePaths.size() > 0) {
			itemX = 0;
			itemsX.clear();
			for (int i = 0; i < mData.getCount(); i++) {
				try {
					JSONObject childData = (JSONObject) ((DoIListData) mData).getData(i);
					int _index = DoTextHelper.strToInt(DoJsonHelper.getString(childData, "template", "0"), -1);
					String templatePath = itemTemplatePaths.get(_index);
					if (templatePath == null) {
						throw new RuntimeException("绑定一个无效的模版Index值");
					}
					DoUIModule uiModule = DoServiceContainer.getUIModuleFactory().createUIModuleBySourceFile(templatePath, this.model.getCurrentPage(), true);
					uiModule.setModelData(childData);
					ItemView itemView = new ItemView(mContext);
					itemView.addView((View) uiModule.getCurrentUIModuleView(), new FrameLayout.LayoutParams((int) uiModule.getRealWidth(), (int) uiModule.getRealHeight()));
					itemView.setOnClickListener(new IndexChanged(i));
					layout.addView(itemView);
					int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
					itemView.measure(w, 0);
					setItemWidth(itemView.getMeasuredWidth());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return layout;
	}

	private void setItemWidth(int itemWidth) {
		itemsX.add(itemX);
		itemX += itemWidth;
	}

	/**
	 * 刷新item数据；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void refreshItems(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		try {
			this.removeAllViews();
			this.addView(getItemView());
		} catch (Exception e) {
			DoServiceContainer.getLogEngine().writeError("refreshItems错误： \t", e);
			e.printStackTrace();
		}
	}

	class IndexChanged implements View.OnClickListener {

		private int itemIndex;

		public IndexChanged(int index) {
			this.itemIndex = index;
		}

		@Override
		public void onClick(View arg0) {
			clickItem(itemIndex);
		}
	}

	class ItemView extends FrameLayout {

		public ItemView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

	}

}