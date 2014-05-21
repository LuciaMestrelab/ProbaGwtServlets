package com.mestrelab.gwt.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.mestrelab.gwt.shared.FieldVerifier;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Proba implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	
	
	private final GreetingServiceAsync greetingService = GWT
			.create(GreetingService.class);
	
	private final VerDatosServiceAsync verDatos = GWT.create(VerDatosService.class);
	
	private final Messages messages = GWT.create(Messages.class);

	private String radioSelected = "";

	private RadioButton radioGender;
	
	private List<String> checkedSelected = new ArrayList<String>();

	public String getRadioSelected() {
		return radioSelected;
	}

	public void setRadioSelected(String radioSelected) {
		this.radioSelected = radioSelected;
	}

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Button sendButton = new Button(messages.sendButton());
		final TextBox nameField = new TextBox();
		nameField.setText(messages.nameField());
		final Label errorLabel = new Label();

		// We can add style names to widgets
		sendButton.addStyleName("sendButton");

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel.get("nameFieldContainer").add(nameField);
		RootPanel.get("sendButtonContainer").add(sendButton);
		RootPanel.get("errorLabelContainer").add(errorLabel);

		// Focus the cursor on the name field when the app loads
		nameField.setFocus(true);
		nameField.selectAll();

		// Create the popup dialog box
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText("Remote Procedure Call");
		dialogBox.setAnimationEnabled(true);
		final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		final Label textToServerLabel = new Label();
		final HTML serverResponseLabel = new HTML();
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
		dialogVPanel.add(textToServerLabel);
		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
		dialogVPanel.add(serverResponseLabel);
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);

		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				sendButton.setEnabled(true);
				sendButton.setFocus(true);
			}
		});

		// Create a handler for the sendButton and nameField
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick(ClickEvent event) {
				sendNameToServer();
			}

			/**
			 * Fired when the user types in the nameField.
			 */
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					sendNameToServer();
				}
			}

			/**
			 * Send the name from the nameField to the server and wait for a
			 * response.
			 */
			private void sendNameToServer() {
				// First, we validate the input.
				errorLabel.setText("");
				String textToServer = nameField.getText();
				if (!FieldVerifier.isValidName(textToServer)) {
					errorLabel.setText("Please enter at least four characters");
					return;
				}

				// Then, we send the input to the server.
				sendButton.setEnabled(false);
				textToServerLabel.setText(textToServer);
				serverResponseLabel.setText("");
				greetingService.greetServer(textToServer,
						new AsyncCallback<String>() {
							public void onFailure(Throwable caught) {
								// Show the RPC error message to the user
								dialogBox
										.setText("Remote Procedure Call - Failure");
								serverResponseLabel
										.addStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML(SERVER_ERROR);
								dialogBox.center();
								closeButton.setFocus(true);
							}

							public void onSuccess(String result) {
								dialogBox.setText("Remote Procedure Call");
								serverResponseLabel
										.removeStyleName("serverResponseLabelError");
								serverResponseLabel.setHTML(result);
								dialogBox.center();
								closeButton.setFocus(true);
							}
						});
			}
		}

		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler(handler);
		nameField.addKeyUpHandler(handler);

		//empeza aki o meu =============================================
		// probas varias
		// radioButton ========================================================
		VerticalPanel panelA = new VerticalPanel();
		HTML label = new HTML("Selecciona un nomes");
		panelA.add(label);

		String[] nomes = { "Lucia", "David", "Ana", "Pablo" };

		for (int i = 0; i < nomes.length; i++) {
			final String VariosNomes = nomes[i];
			final RadioButton radio = new RadioButton("Nomes", VariosNomes);

			if (i >= 2) {
				radio.setEnabled(false);
			}
			radio.addClickHandler(new ClickHandler() {

				public void onClick(ClickEvent arg0) {
					setRadioSelected(radio.getText());

				}
			});
			
			panelA.add(radio);
			panelA.setStyleName("col-md-3");
			RootPanel.get("CallRadioButtons").add(panelA);

		}

		// checkBox ==================================================
		VerticalPanel panelB = new VerticalPanel();
		HTML label2 = new HTML("Selecciona un ou varios apelidos");
		panelB.add(label2);

		String[] apellidos = { "Chas", "Alvarez", "Valiñas", "Pereiras" };

		for (int i = 0; i < apellidos.length; i++) {
			final String ape = apellidos[i];
			final CheckBox checkbox = new CheckBox(ape);
			checkbox.addClickHandler(new ClickHandler() {

				public void onClick(ClickEvent arg0) {
					if (checkbox.getValue()) {
						if (checkedSelected.size() >= 1) {
							checkedSelected.add(checkbox.getText());

						} else {
							checkedSelected.add(checkbox.getText());
						}
					} else {
						// borralo da lista
						checkedSelected.clear();
					}

				}
			});
			panelB.add(checkbox);
			panelB.setStyleName("col-md-3");
			RootPanel.get("CallCheckbox").add(panelB);
			
		}

		// boton normal ========================================================
		HTML label3 = new HTML("Pulsa o boton");
		RootPanel.get("CallBasicButtons").add(label3);

		Button normalButton = new Button("Aceptar", new ClickHandler() {

			public void onClick(ClickEvent arg0) {
				// TODO Auto-generated method stub
				Window.alert("Nome Seleccionado: " + radioSelected
						+ "\nApelidos Seleccionados: " + checkedSelected);
			}
		});
		normalButton.setStyleName("col-md-2");
		RootPanel.get("CallBasicButtons").add(normalButton);
		
		// file upload ========================================================
		VerticalPanel panelC = new VerticalPanel();
		HTML label4 = new HTML("File upload");
		panelC.add(label4);

		final FileUpload fileUpload = new FileUpload();
		panelC.add(fileUpload);

		// boton para subilo ========================================================
		Button uploadButton = new Button("Upload File");
		uploadButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent arg0) {
				// TODO Auto-generated method stub
				String fileName = fileUpload.getFilename();
				if (fileName.length() == 0) {
					Window.alert("Error o subir o ficheiro");
				} else {
					Window.alert("Ficheiro subido correctamente");
				}
			}
		});
	
		panelC.add(uploadButton);
		panelC.setStyleName("col-md-6");
		RootPanel.get("CallFileUpload").add(panelC);
		

		// Date picker ========================================================
		VerticalPanel panelD = new VerticalPanel();
		HTML label6 = new HTML("Date picker");
		panelD.add(label6);

		DatePicker datePic = new DatePicker();
		final Label text = new Label();

		datePic.addValueChangeHandler(new ValueChangeHandler<Date>() {

			public void onValueChange(ValueChangeEvent<Date> arg0) {
				// TODO Auto-generated method stub
				Date date = arg0.getValue();
				@SuppressWarnings("deprecation")
				String dateString = DateTimeFormat.getMediumDateFormat()
						.format(date);
				text.setText(dateString);
			}
		});

		datePic.setValue(new Date(), true);
		
		panelD.add(text);
		panelD.add(datePic);
		panelD.setStyleName("col-md-3");
		RootPanel.get("CallDatePicker").add(panelD);
		
		// crear o dateBox ========================================================
		VerticalPanel panelDD = new VerticalPanel();
		@SuppressWarnings("deprecation")
		DateTimeFormat dateFormat = DateTimeFormat.getLongDateFormat();
		DateBox dateBox = new DateBox();
		dateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
		dateBox.setValue(new Date(), true);
		
		HTML label61 = new HTML("DateBox");
		panelDD.add(label61);
		panelDD.add(dateBox);
		panelDD.setStyleName("col-md-2");
	
		RootPanel.get("CallDatePicker").add(panelDD);

		
		// Hyperlink ========================================================
		VerticalPanel panelE = new VerticalPanel();
		HTML label7 = new HTML("Hyperlink");
		panelE.add(label7);
		
		Hyperlink link = new Hyperlink("Link RadioButtons", "RadioButtons");
		Hyperlink link1 = new Hyperlink("Link Checkbox", "Checkbox");
		Hyperlink link2 = new Hyperlink("Link Basic Buttons", "Basic Buttons");
		Hyperlink link3 = new Hyperlink("Link File Upload", "File Upload");
		Hyperlink link4 = new Hyperlink("Link Date Picker", "Date Picker");

		panelE.add(link);
		panelE.add(link1);
		panelE.add(link2);
		panelE.add(link3);
		panelE.add(link4);
		panelE.setStyleName("col-md-3");

		RootPanel.get("CallHyperlink").add(panelE);

		// ListBox ========================================================
		VerticalPanel panelF = new VerticalPanel();
		HTML label88 = new HTML("List Box");
		panelF.add(label88);
		
		final ListBox dropBox = new ListBox(false);
		String[] listTypes = {"A", "B", "C", "D" };

		for (int i = 0; i < listTypes.length; i++) {
			dropBox.addItem(listTypes[i]);
		}
		panelF.add(dropBox);
		panelF.setStyleName("col-md-3");
		RootPanel.get("CallListBox").add(panelF);
		
		//lista multiple seleccion ========================================================
		VerticalPanel panelG = new VerticalPanel();
		HTML label99 = new HTML("Multi Box");
		panelG.add(label99);
		
		final ListBox multiBox = new ListBox(true);
		multiBox.setWidth("8em");
		multiBox.setVisibleItemCount(5);
		
		dropBox.addChangeHandler(new ChangeHandler() {
			
			public void onChange(ChangeEvent arg0) {
				// TODO Auto-generated method stub
				showCategory(multiBox, dropBox.getSelectedIndex());
			}
		});
		
		showCategory(multiBox, 0);
		
		panelG.add(multiBox);
		panelG.setStyleName("col-md-3");
		RootPanel.get("CallMultiBox").add(panelG);
		
	
		//Suggest box ========================================================
		MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
		String[] words = {"ora","cle","oracle","lucia","chas","david","aooooppppp"};
		for(int i=0; i<words.length; i++){
			oracle.add(words[i]);
		}
		
		final SuggestBox suggestBox = new SuggestBox(oracle);
		VerticalPanel panelH = new VerticalPanel();
		HTML label1 = new HTML("Suggest Box");
		panelH.add(label1);
		panelH.add(suggestBox);
		panelH.setStyleName("col-md-2");
		RootPanel.get("CallSuggestBox").add(panelH);
	
		
		//static tree ========================================================
		Tree staticTree = createStaticTree();
		staticTree.setAnimationEnabled(true);
		ScrollPanel staticTreeWrapper = new ScrollPanel(staticTree);
		//staticTreeWrapper.setSize("300px", "300px");
		staticTreeWrapper.setHeight("300px");
		
		DecoratorPanel staticDecorator = new DecoratorPanel();
		staticDecorator.setWidget(staticTreeWrapper);
		
		VerticalPanel panelJ = new VerticalPanel();
		HTML labelJ = new HTML("Static Tree");
		panelJ.add(labelJ);
		panelJ.add(staticTree);
		panelJ.setStyleName("col-md-3");
		RootPanel.get("CallStaticTree").add(panelJ);
		
		
		//menu bar ========================================================
		//crear comando que se execute o seleccionar un menu item
		Command menuCommand = new Command(){
			private int curPhrase = 0;
			private final String[] phrases = {"File","Edit","GWT","Help"};
			
			public void execute(){
				Window.alert(phrases[curPhrase]);
				curPhrase = (curPhrase + 1) % phrases.length;
			}
		};
		
		//crear menu bar
		MenuBar menu = new MenuBar();
		menu.setAutoOpen(true);
		//menu.setWidth("100px");
		menu.addStyleName("col-md-3");
		menu.setAnimationEnabled(true);
		
		//crear submenu
		MenuBar recentDocsMenu = new MenuBar(true);
		String[] recentDocs = {"Fishing in the desert.txt", "How to tame a wild parrot", "Idiots guide to emu farms"};
		for(int i=0; i<recentDocs.length; i++){
			recentDocsMenu.addItem(recentDocs[i], menuCommand);
		}
		
		//crear o fileMenu
		MenuBar fileMenu  = new MenuBar(true);
		fileMenu.setAnimationEnabled(true);
		String[] menuFile = {"New","Open","Close","Recent","Exit"};
		menu.addItem(new MenuItem("File", fileMenu));
		for(int i=0; i<menuFile.length; i++){
			if(i == 3){
				fileMenu.addSeparator();
				fileMenu.addItem(menuFile[i], recentDocsMenu);
				fileMenu.addSeparator();
			}else{
				fileMenu.addItem(menuFile[i], menuCommand);
			}
		}
		
		//crear o editMenu
		MenuBar editMenu = new MenuBar(true);
		menu.addItem(new MenuItem("Edit", editMenu));
		String[] editOptions = {"Undo","Redo","Cut","Copy","Paste"};
		for(int i=0; i<editOptions.length; i++){
			editMenu.addItem(editOptions[i], menuCommand);
		}
		
		//crear gwt menu
		MenuBar gwtMenu = new MenuBar(true);
		menu.addItem(new MenuItem("GWT", true, gwtMenu));
		String[] gwtOptions = {"Download","Examples","Source Code","GWD wit' the program"};
		for(int i=0; i<gwtOptions.length; i++){
			gwtMenu.addItem(gwtOptions[i], menuCommand);
		}
		
		//crear help menu
		MenuBar helpMenu = new MenuBar(true);
		menu.addSeparator();
		menu.addItem(new MenuItem("Help",helpMenu));
		String[] helpOptions = {"Contents","Fortune Cookie","About GWT"};
		for(int i=0; i<helpOptions.length; i++){
			helpMenu.addItem(helpOptions[i], menuCommand);
		}
		
		
		VerticalPanel panelK = new VerticalPanel();
		HTML labelK = new HTML("Menu bar");
		panelK.add(labelK);
		panelK.add(menu);
//		panelK.add(fileMenu);
//		panelK.add(editMenu);
//		panelK.add(gwtMenu);
//		panelK.add(helpMenu);
//		panelK.setStyleName("col-md-4");
		RootPanel.get("CallMenuBar").add(menu);
		
		
		//disclosure panel ========================================================
		VerticalPanel panelL = new VerticalPanel();
		panelL.setSpacing(8);
		
		FlexTable layout = new FlexTable();
		layout.setCellSpacing(6);
		layout.setWidth("300px");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();
		
		//titulo
		layout.setHTML(0,0,"Enter Search Criteria");
		cellFormatter.setColSpan(0, 0, 2);
		cellFormatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		//Añadir opcions
		layout.setHTML(1, 0, "Name: ");
		final TextBox textToName2 = new TextBox();
		layout.setWidget(1, 1, textToName2);
		layout.setHTML(2, 0, "Description: ");
		final TextBox textDescription2 = new TextBox();
		layout.setWidget(2, 1, textDescription2);
		final Label textGender = new Label();
		//crear mais opcions
		HorizontalPanel genderPanel = new HorizontalPanel();
		final String[] genderOptions = {"male","female"};
		for(int i=0; i<genderOptions.length; i++){
			radioGender = new RadioButton("Gender", genderOptions[i]);
			radioGender.addClickHandler(new ClickHandler() {
				
				public void onClick(ClickEvent arg0) {
					textGender.setText(((RadioButton)arg0.getSource()).getText());					
				}
			});
			genderPanel.add(radioGender);
			
			//genderPanel.add(new RadioButton("Gender: ", genderOptions[i]));
		}
		
		
		Grid advancedOptions = new Grid(2,2);
		advancedOptions.setCellSpacing(6);
		advancedOptions.setHTML(0, 0, "Location: ");
		final TextBox textLocation2 = new TextBox();
		advancedOptions.setWidget(0, 1, textLocation2);
		advancedOptions.setHTML(1, 0, "Gender: ");
		advancedOptions.setWidget(1, 1, genderPanel);
		
		DisclosurePanel advanceDisclosure = new DisclosurePanel("Advance Criteria");
		advanceDisclosure.setAnimationEnabled(true);
		advanceDisclosure.setContent(advancedOptions);
		layout.setWidget(3, 0, advanceDisclosure);
		cellFormatter.setColSpan(3, 0, 2);
		
		DecoratorPanel decPanel = new DecoratorPanel();
		decPanel.setWidget(layout);
		panelL.add(decPanel);
		
		
		RootPanel.get("CallDisclosurePanel").add(panelL);
		

		
		//intento de facer cuadro de dialogo
		//========================================================
		
		final Button datosButton = new Button("Ver datos");
		RootPanel.get("CallButton").add(datosButton);
		
		//crear o dialogo
		final DialogBox dialogBox2 = new DialogBox();
		dialogBox2.setText("Visualizar datos");
		dialogBox2.setAnimationEnabled(true);
		final Button closeButton2 = new Button("Close");
		closeButton2.getElement().setId("closeButton2");
		final Label textToName = new Label();
		final Label textToDescription = new Label();
		final Label textToLocation = new Label();
		final Label textSaludo = new Label();
		
		VerticalPanel dialogPanel = new VerticalPanel();
		dialogPanel.addStyleName("dialogVPanel");
		dialogPanel.add(new HTML("<b>Name: </b>"));
		dialogPanel.add(textToName);
		dialogPanel.add(new HTML("<b>Saludo: </b>"));
		dialogPanel.add(textSaludo);
		dialogPanel.add(new HTML("<b>Description: </b>"));
		dialogPanel.add(textToDescription);
		dialogPanel.add(new HTML("<b>Location: </b>"));
		dialogPanel.add(textToLocation);
		dialogPanel.add(new HTML("<b>Gender: </b>"));
		dialogPanel.add(textGender);
		
		dialogPanel.add(new HTML("<br></br>"));
		dialogPanel.add(closeButton2);
		dialogBox2.setWidget(dialogPanel);
		
		closeButton2.addClickHandler(new ClickHandler() {
			
			public void onClick(ClickEvent arg0) {
				// TODO Auto-generated method stub
				dialogBox2.hide();
				datosButton.setEnabled(true);
				datosButton.setFocus(true);
			}
		});
		
		class MyHandler2 implements ClickHandler, KeyUpHandler{

			public void onKeyUp(KeyUpEvent event) {
				// TODO Auto-generated method stub
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					envioDatos();
				}
			}

			public void onClick(ClickEvent arg0) {
				// TODO Auto-generated method stub
				
				envioDatos();
			}
			
			private void envioDatos(){
				errorLabel.setText("");
				String textToServer = textToName2.getText();
				if (!FieldVerifier.isValidName(textToServer)) {
					errorLabel.setText("Please enter at least four characters");
					return;
				}
				
				datosButton.setEnabled(false);
				textToName.setText(textToServer);
				textSaludo.setText("");
				textToDescription.setText("");
				textToLocation.setText("");
				
				verDatos.verDatos(textToServer, new AsyncCallback<String>() {
					
					public void onSuccess(String arg0) {
						// TODO Auto-generated method stub
						dialogBox2.setText("Remote procedure call");						
						textSaludo.setText(arg0);
						String description = textDescription2.getText();
						textToDescription.setText(description);
						String location = textLocation2.getText();
						textToLocation.setText(location);
						dialogBox2.center();
						closeButton2.setFocus(true);
					}
					
					public void onFailure(Throwable arg0) {
						// TODO Auto-generated method stub
						dialogBox2.setText("Remote procedure call - Failure");
						//textToDescription.addStyleName("Error!!");
						textDescription2.setText("ERROR!!!:" +arg0.getMessage());
						dialogBox2.center();
						closeButton2.setFocus(true);
					}
				});
			}
		}
		
		MyHandler2 han = new MyHandler2();
		datosButton.addClickHandler(han);
		
	}

	
	//========================================================

	private Tree createStaticTree(){
		Tree staticTree = new Tree();
		String[] beet = {"as","sd","sd"};
		TreeItem beethovenItem = staticTree.addTextItem("Beethoven");
		addMusicSection(beethovenItem, "concertos", beet);
		addMusicSection(beethovenItem, "Cuartetos", beet);
		addMusicSection(beethovenItem, "Sonatas", beet);
		
		String[] mozart = {"as","sd","sd"};
		TreeItem mozartItem = staticTree.addTextItem("Mozart");
		addMusicSection(mozartItem, "Concertos", mozart);
		addMusicSection(mozartItem, "Cuartetos", mozart);
		addMusicSection(mozartItem, "Sonatas", mozart);
		addMusicSection(mozartItem, "Sinfonias", mozart);
		
		return staticTree;
	}
	
	private void addMusicSection(TreeItem parent, String label, String[] composerWorks){
		TreeItem section = parent.addTextItem(label);
		for(String work : composerWorks){
			section.addTextItem(work);
		}
	}
	private void showCategory(ListBox listBox, int category){
		listBox.clear();
		String[] listData = null;
		String[] datos = {"A.A","A.B","A.C"};
		String[] datos1 = {"B.A","B.B","B.C"};
		String[] datos2 = {"C.A","C.B","C.C"};
		
		switch (category) {
		case 0:
			listData = datos;
			break;
		case 1:
			listData = datos1;
			break;
		case 2:
			listData = datos2;
			break;
		}
		for(int i=0; i<listData.length; i++){
			listBox.addItem(listData[i]);
		}
	}
	
	
	
	
	public List<String> getCheckedSelected() {
		return checkedSelected;
	}

	public void setCheckedSelected(List<String> checkedSelected) {
		this.checkedSelected = checkedSelected;
	}
}
