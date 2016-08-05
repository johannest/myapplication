package com.example.myapplication;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * This UI is the application entry point. A UI may either represent a browser
 * window (or tab) or some part of a html page where a Vaadin application is
 * embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is
 * intended to be overridden to add component to the user interface and
 * initialize non-component functionality.
 */
@Theme("mytheme")
@Widgetset("com.example.myapplication.MyAppWidgetset")
@Push
public class MyUI extends UI {
	
	@Override
	protected void init(VaadinRequest vaadinRequest) {
		final VerticalLayout layout = new VerticalLayout();
		layout.addComponents(createMethodPropertyExample());
		layout.addComponent(createFieldGroupExample());
		layout.addComponent(createPushExample());
		layout.addComponent(createTableExample());
		layout.setMargin(true);
		layout.setSpacing(true);
		setContent(layout);
	}

	private Component createMethodPropertyExample() {
		final ExampleDTO pojo = new ExampleDTO("-","-");
		// bind to setMyName() and getMyName()
		MethodProperty<String> myProperty = new MethodProperty<>(pojo, "myName");

		TextField tf = new TextField(myProperty);
		
		ComboBox sf = new ComboBox();
		sf.setEnabled(true);
		sf.setNewItemsAllowed(true);
		sf.addItem("Item 1");
		sf.addItem("Item 2");
		sf.addItem("Item 3");
		sf.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				// will call pojo.setMyName("New Name"); internally and will make tf
				// to update its value
				myProperty.setValue((String) event.getProperty().getValue());
			}
		});
		HorizontalLayout horizontalLayout = new HorizontalLayout(new Label("Using MethodProperty"),sf,tf);
		horizontalLayout.setSpacing(true);
		return horizontalLayout;
	}
	
	private Component createFieldGroupExample() {
		final ExampleDTO pojo = new ExampleDTO("-", "-");
		
		final BeanItem<ExampleDTO> beanItem = new BeanItem<ExampleDTO>(pojo);
		// both BeanFieldGroup and FieldGroup seem to work in this case, BeanFieldGroup has some helper methods
		//final FieldGroup binder = new FieldGroup(beanItem);
		
		final BeanFieldGroup<ExampleDTO> binder = new BeanFieldGroup<ExampleDTO>(ExampleDTO.class);
		binder.setItemDataSource(beanItem);
		
		ComboBox sf = new ComboBox();
		sf.setEnabled(true);
		sf.setImmediate(true);
		sf.setNewItemsAllowed(true);
		sf.addItem("Item 1");
		sf.addItem("Item 2");

		sf.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				pojo.setMyName("My name - "+sf.getValue());
				pojo.setAddress("My address "+sf.getValue());
				
				// how to set property readOnly if the suggestion value is "Item 2"
				beanItem.getItemProperty("address").setReadOnly("Item 2".equals(sf.getValue()));
				binder.setItemDataSource(beanItem);
				
				// also this works with BeanFieldGroup
				//binder.setItemDataSource(pojo);
			}
		});
		
		class CustomFormLayout extends FormLayout {
			@PropertyId("myName")
			TextField nameField = new TextField("My Name");
			@PropertyId("address")
			TextField addresField = new TextField("Address");
			
			public CustomFormLayout() {
				addComponent(nameField);
				addComponent(addresField);
			}
		}
		
		CustomFormLayout formLayout = new CustomFormLayout();
		binder.bindMemberFields(formLayout);
		
		HorizontalLayout horizontalLayout = new HorizontalLayout(new Label("Using FieldGroup"),sf,formLayout);
		horizontalLayout.setSpacing(true);
		return horizontalLayout;
	}
	
	private Component createPushExample() {
		Button b = new Button("Start bakcgroud thread");
		HorizontalLayout horizontalLayout = new HorizontalLayout(new Label("Push example"),b);
		
		b.addClickListener(new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				Thread t = new Thread() {

					@Override
					public void run() {
						while(true) {
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							// UI.getCurrent().access is needed if the update happen from another thread
							UI.getCurrent().access(new Runnable() {
								@Override
								public void run() {
									horizontalLayout.addComponent(new Label("Label added asychronously"));
								}
							});
							
						}
					}
					
				};
				t.start();
			}
		});
		
		horizontalLayout.setSpacing(true);
		return horizontalLayout;
	}


	private Component createTableExample() {
		BeanItemContainer<ExampleDTO> beans =
	            new BeanItemContainer<ExampleDTO>(ExampleDTO.class);
		beans.addBean(new ExampleDTO("adsad","asdasda a1"));
		beans.addBean(new ExampleDTO("qweqw","qweqweqw b10"));
		
		
		Button b = new Button("Add item to table");
		b.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				beans.addBean(new ExampleDTO("rwerwerwerwer","ewrwe d2"));
			}
		});
		Button b2 = new Button("Remove first item from table");
		b2.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				beans.removeItem(beans.getIdByIndex(0));
			}
		});
		
		Table t = new Table();
		t.setContainerDataSource(beans);

		HorizontalLayout horizontalLayout = new HorizontalLayout(new Label("Table with BeanItemContainer"), b, b2,
				t);
		horizontalLayout.setSpacing(true);
		return horizontalLayout;
	}
	
	public class ExampleDTO {
		private String myName;
		private String address;

		public ExampleDTO(String myName, String address) {
			this.myName = myName;
			this.address = address;
		}

		public String getMyName() {
			return myName;
		}

		public void setMyName(String myName) {
			this.myName = myName;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}
	}

	@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
	public static class MyUIServlet extends VaadinServlet {
	}
}
