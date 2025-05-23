/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.mail.workflow.actions.getpop;

import jakarta.mail.Folder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.Props;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.mail.metadata.MailServerConnection;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.dialog.BaseDialog;
import org.apache.hop.ui.core.dialog.ErrorDialog;
import org.apache.hop.ui.core.dialog.MessageBox;
import org.apache.hop.ui.core.gui.GuiResource;
import org.apache.hop.ui.core.widget.MetaSelectionLine;
import org.apache.hop.ui.core.widget.PasswordTextVar;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.pipeline.transform.BaseTransformDialog;
import org.apache.hop.ui.workflow.action.ActionDialog;
import org.apache.hop.ui.workflow.dialog.WorkflowDialog;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/** This dialog allows you to edit the Get POP action settings. */
public class ActionGetPOPDialog extends ActionDialog {
  private static final Class<?> PKG = ActionGetPOP.class;
  private static final String CONST_OK = "System.Button.OK";

  private Text wName;
  private TextVar wServerName;
  private TextVar wSender;
  private TextVar wRecipient;
  private TextVar wSubject;
  private TextVar wBody;
  private Label wlAttachmentFolder;
  private TextVar wAttachmentFolder;
  private Button wbAttachmentFolder;
  private Label wlAttachmentWildcard;
  private TextVar wAttachmentWildcard;
  private TextVar wUserName;
  private Label wlIMAPFolder;
  private TextVar wIMAPFolder;
  private Label wlMoveToFolder;
  private TextVar wMoveToFolder;
  private Button wSelectMoveToFolder;
  private Button wTestMoveToFolder;
  private TextVar wPassword;
  private Label wlOutputDirectory;
  private TextVar wOutputDirectory;
  private Label wlFilenamePattern;
  private TextVar wFilenamePattern;
  private Button wbDirectory;
  private Label wlListmails;
  private CCombo wListmails;
  private Label wlIMAPListmails;
  private CCombo wIMAPListmails;
  private Label wlAfterGetIMAP;
  private CCombo wAfterGetIMAP;
  private Label wlFirstmails;
  private TextVar wFirstmails;
  private Label wlIMAPFirstmails;
  private TextVar wIMAPFirstmails;
  private TextVar wPort;
  private Button wUseSSL;
  private Button wUseXOAUTH2;
  private Button wUseProxy;
  private Label wlProxyUsername;
  private TextVar wProxyUsername;
  private Label wlIncludeSubFolders;
  private Button wIncludeSubFolders;
  private Label wlCreateMoveToFolder;
  private Button wCreateMoveToFolder;
  private Label wlCreateLocalFolder;
  private Button wCreateLocalFolder;
  private Button wNegateSender;
  private Button wNegateReceipient;
  private Button wNegateSubject;
  private Button wNegateBody;
  private Button wNegateReceivedDate;
  private Label wlGetAttachment;
  private Button wGetAttachment;
  private Label wlGetMessage;
  private Button wGetMessage;
  private Label wlDifferentFolderForAttachment;
  private Button wDifferentFolderForAttachment;
  private Label wlPOP3Message;
  private Label wlDelete;
  private Button wDelete;
  private ActionGetPOP action;
  private boolean changed;
  private Label wlReadFrom;
  private TextVar wReadFrom;
  private Button open;
  private Label wlConditionOnReceivedDate;
  private CCombo wConditionOnReceivedDate;
  private CCombo wActionType;
  private Label wlReadTo;
  private TextVar wReadTo;
  private Button opento;
  private CCombo wProtocol;
  private Button wTestIMAPFolder;
  private Button wSelectFolder;
  private MailConnection mailConn = null;
  private MetaSelectionLine wSelectionLine;

  public ActionGetPOPDialog(
      Shell parent, ActionGetPOP action, WorkflowMeta workflowMeta, IVariables variables) {
    super(parent, workflowMeta, variables);
    this.action = action;
    if (this.action.getName() == null) {
      this.action.setName(BaseMessages.getString(PKG, "ActionGetPOP.Name.Default"));
    }
  }

  @Override
  public IAction open() {

    shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE);
    PropsUi.setLook(shell);
    WorkflowDialog.setShellImage(shell, action);

    ModifyListener lsMod =
        e -> {
          closeMailConnection();
          action.setChanged();
        };

    SelectionListener lsSelection =
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            action.setChanged();
            closeMailConnection();
          }
        };
    changed = action.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = PropsUi.getFormMargin();
    formLayout.marginHeight = PropsUi.getFormMargin();

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "ActionGetPOP.Title"));

    int middle = props.getMiddlePct();
    int margin = PropsUi.getMargin();

    // Buttons go at the very bottom
    //
    Button wOk = new Button(shell, SWT.PUSH);
    wOk.setText(BaseMessages.getString(PKG, CONST_OK));
    wOk.addListener(SWT.Selection, e -> ok());
    Button wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
    wCancel.addListener(SWT.Selection, e -> cancel());
    BaseTransformDialog.positionBottomButtons(shell, new Button[] {wOk, wCancel}, margin, null);

    // Filename line
    Label wlName = new Label(shell, SWT.RIGHT);
    wlName.setText(BaseMessages.getString(PKG, "ActionGetPOP.Name.Label"));
    PropsUi.setLook(wlName);
    FormData fdlName = new FormData();
    fdlName.left = new FormAttachment(0, 0);
    fdlName.right = new FormAttachment(middle, -margin);
    fdlName.top = new FormAttachment(0, margin);
    wlName.setLayoutData(fdlName);
    wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wName);
    wName.addModifyListener(lsMod);
    FormData fdName = new FormData();
    fdName.left = new FormAttachment(middle, 0);
    fdName.top = new FormAttachment(0, margin);
    fdName.right = new FormAttachment(100, 0);
    wName.setLayoutData(fdName);

    CTabFolder wTabFolder = new CTabFolder(shell, SWT.BORDER);
    PropsUi.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

    // ////////////////////////
    // START OF GENERAL TAB ///
    // ////////////////////////

    CTabItem wGeneralTab = new CTabItem(wTabFolder, SWT.NONE);
    wGeneralTab.setFont(GuiResource.getInstance().getFontDefault());
    wGeneralTab.setText(BaseMessages.getString(PKG, "ActionGetPOP.Tab.General.Label"));
    Composite wGeneralComp = new Composite(wTabFolder, SWT.NONE);
    PropsUi.setLook(wGeneralComp);
    FormLayout generalLayout = new FormLayout();
    generalLayout.marginWidth = 3;
    generalLayout.marginHeight = 3;
    wGeneralComp.setLayout(generalLayout);

    // ////////////////////////
    // START OF CONNECTION LINE GROUP
    // /////////////////////////

    Group wConnectionGroup = new Group(wGeneralComp, SWT.SHADOW_NONE);
    PropsUi.setLook(wConnectionGroup);
    wConnectionGroup.setText(BaseMessages.getString(PKG, "ActionGetPOP.Connection.Group.Label"));
    FormLayout connectionGroupLayout = new FormLayout();
    connectionGroupLayout.marginWidth = 10;
    connectionGroupLayout.marginHeight = 10;
    wConnectionGroup.setLayout(connectionGroupLayout);

    wSelectionLine =
        new MetaSelectionLine(
            variables,
            metadataProvider,
            MailServerConnection.class,
            wConnectionGroup,
            SWT.SINGLE | SWT.LEFT | SWT.BORDER,
            BaseMessages.getString(PKG, "ActionGetPOP.Connection.Label"),
            BaseMessages.getString(PKG, "ActionGetPOP.Connection.ToolTip"));
    PropsUi.setLook(wSelectionLine);
    FormData fdSelectionLine = new FormData();
    fdSelectionLine.left = new FormAttachment(0, 0);
    fdSelectionLine.top = new FormAttachment(wGeneralComp, 0);
    fdSelectionLine.right = new FormAttachment(100, -margin);
    wSelectionLine.setLayoutData(fdSelectionLine);
    wSelectionLine.addListener(SWT.Selection, e -> action.setChanged(true));
    try {
      wSelectionLine.fillItems();
    } catch (Exception e) {
      new ErrorDialog(shell, "Error", "Error getting list of Mail Server connectioons", e);
    }

    FormData fdConnectionGroup = new FormData();
    fdConnectionGroup.left = new FormAttachment(0, 0);
    fdConnectionGroup.top = new FormAttachment(wName, margin);
    fdConnectionGroup.right = new FormAttachment(100, 0);
    wConnectionGroup.setLayoutData(fdConnectionGroup);

    // ////////////////////////
    // START OF SERVER SETTINGS GROUP///
    // /
    Group wServerSettings = new Group(wGeneralComp, SWT.SHADOW_NONE);
    PropsUi.setLook(wServerSettings);
    wServerSettings.setText(BaseMessages.getString(PKG, "ActionGetPOP.ServerSettings.Group.Label"));

    FormLayout serverSettingsgroupLayout = new FormLayout();
    serverSettingsgroupLayout.marginWidth = 10;
    serverSettingsgroupLayout.marginHeight = 10;
    wServerSettings.setLayout(serverSettingsgroupLayout);

    // ServerName line
    Label wlServerName = new Label(wServerSettings, SWT.RIGHT);
    wlServerName.setText(BaseMessages.getString(PKG, "ActionGetPOP.Server.Label"));
    PropsUi.setLook(wlServerName);
    FormData fdlServerName = new FormData();
    fdlServerName.left = new FormAttachment(0, 0);
    fdlServerName.top = new FormAttachment(wSelectionLine, 2 * margin);
    fdlServerName.right = new FormAttachment(middle, -margin);
    wlServerName.setLayoutData(fdlServerName);
    wServerName = new TextVar(variables, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wServerName);
    wServerName.addModifyListener(lsMod);
    FormData fdServerName = new FormData();
    fdServerName.left = new FormAttachment(middle, 0);
    fdServerName.top = new FormAttachment(wSelectionLine, margin);
    fdServerName.right = new FormAttachment(100, 0);
    wServerName.setLayoutData(fdServerName);

    // USE connection with SSL
    Label wlUseSSL = new Label(wServerSettings, SWT.RIGHT);
    wlUseSSL.setText(BaseMessages.getString(PKG, "ActionGetPOP.UseSSLMails.Label"));
    PropsUi.setLook(wlUseSSL);
    FormData fdlUseSSL = new FormData();
    fdlUseSSL.left = new FormAttachment(0, 0);
    fdlUseSSL.top = new FormAttachment(wServerName, margin);
    fdlUseSSL.right = new FormAttachment(middle, -margin);
    wlUseSSL.setLayoutData(fdlUseSSL);
    wUseSSL = new Button(wServerSettings, SWT.CHECK);
    PropsUi.setLook(wUseSSL);
    FormData fdUseSSL = new FormData();
    wUseSSL.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.UseSSLMails.Tooltip"));
    fdUseSSL.left = new FormAttachment(middle, 0);
    fdUseSSL.top = new FormAttachment(wlUseSSL, 0, SWT.CENTER);
    fdUseSSL.right = new FormAttachment(100, 0);
    wUseSSL.setLayoutData(fdUseSSL);

    wUseSSL.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            closeMailConnection();
            refreshPort(true);
          }
        });

    // USE connection with XOAUTH2
    Label wlUseXOAUTH2 = new Label(wServerSettings, SWT.RIGHT);
    wlUseXOAUTH2.setText(BaseMessages.getString(PKG, "ActionGetPOP.UseXOAUTH2Mails.Label"));
    PropsUi.setLook(wlUseXOAUTH2);
    FormData fdlUseXOAUTH2 = new FormData();
    fdlUseXOAUTH2.left = new FormAttachment(0, 0);
    fdlUseXOAUTH2.top = new FormAttachment(wUseSSL, margin);
    fdlUseXOAUTH2.right = new FormAttachment(middle, -margin);
    wlUseXOAUTH2.setLayoutData(fdlUseXOAUTH2);
    wUseXOAUTH2 = new Button(wServerSettings, SWT.CHECK);
    PropsUi.setLook(wUseXOAUTH2);
    FormData fdUseXOAUTH2 = new FormData();
    wUseXOAUTH2.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.UseXOAUTH2Mails.Tooltip"));
    fdUseXOAUTH2.left = new FormAttachment(middle, 0);
    fdUseXOAUTH2.top = new FormAttachment(wUseSSL, margin);
    fdUseXOAUTH2.right = new FormAttachment(100, 0);
    wUseXOAUTH2.setLayoutData(fdUseXOAUTH2);

    wUseXOAUTH2.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            closeMailConnection();
            refreshPort(true);
          }
        });

    // port
    Label wlPort = new Label(wServerSettings, SWT.RIGHT);
    wlPort.setText(BaseMessages.getString(PKG, "ActionGetPOP.SSLPort.Label"));
    PropsUi.setLook(wlPort);
    FormData fdlPort = new FormData();
    fdlPort.left = new FormAttachment(0, 0);
    fdlPort.top = new FormAttachment(wlUseXOAUTH2, 2 * margin);
    fdlPort.right = new FormAttachment(middle, -margin);
    wlPort.setLayoutData(fdlPort);
    wPort = new TextVar(variables, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wPort);
    wPort.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.SSLPort.Tooltip"));
    wPort.addModifyListener(lsMod);
    FormData fdPort = new FormData();
    fdPort.left = new FormAttachment(middle, 0);
    fdPort.top = new FormAttachment(wlPort, 0, SWT.CENTER);
    fdPort.right = new FormAttachment(100, 0);
    wPort.setLayoutData(fdPort);

    // UserName line
    Label wlUserName = new Label(wServerSettings, SWT.RIGHT);
    wlUserName.setText(BaseMessages.getString(PKG, "ActionGetPOP.Username.Label"));
    PropsUi.setLook(wlUserName);
    FormData fdlUserName = new FormData();
    fdlUserName.left = new FormAttachment(0, 0);
    fdlUserName.top = new FormAttachment(wPort, margin);
    fdlUserName.right = new FormAttachment(middle, -margin);
    wlUserName.setLayoutData(fdlUserName);
    wUserName = new TextVar(variables, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wUserName);
    wUserName.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.Username.Tooltip"));
    wUserName.addModifyListener(lsMod);
    FormData fdUserName = new FormData();
    fdUserName.left = new FormAttachment(middle, 0);
    fdUserName.top = new FormAttachment(wPort, margin);
    fdUserName.right = new FormAttachment(100, 0);
    wUserName.setLayoutData(fdUserName);

    // Password line
    Label wlPassword = new Label(wServerSettings, SWT.RIGHT);
    wlPassword.setText(BaseMessages.getString(PKG, "ActionGetPOP.Password.Label"));
    PropsUi.setLook(wlPassword);
    FormData fdlPassword = new FormData();
    fdlPassword.left = new FormAttachment(0, 0);
    fdlPassword.top = new FormAttachment(wUserName, margin);
    fdlPassword.right = new FormAttachment(middle, -margin);
    wlPassword.setLayoutData(fdlPassword);
    wPassword = new PasswordTextVar(variables, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wPassword);
    wPassword.addModifyListener(lsMod);
    FormData fdPassword = new FormData();
    fdPassword.left = new FormAttachment(middle, 0);
    fdPassword.top = new FormAttachment(wUserName, margin);
    fdPassword.right = new FormAttachment(100, 0);
    wPassword.setLayoutData(fdPassword);

    // USE proxy
    Label wlUseProxy = new Label(wServerSettings, SWT.RIGHT);
    wlUseProxy.setText(BaseMessages.getString(PKG, "ActionGetPOP.UseProxyMails.Label"));
    PropsUi.setLook(wlUseProxy);
    FormData fdlUseProxy = new FormData();
    fdlUseProxy.left = new FormAttachment(0, 0);
    fdlUseProxy.top = new FormAttachment(wPassword, 2 * margin);
    fdlUseProxy.right = new FormAttachment(middle, -margin);
    wlUseProxy.setLayoutData(fdlUseProxy);
    wUseProxy = new Button(wServerSettings, SWT.CHECK);
    PropsUi.setLook(wUseProxy);
    FormData fdUseProxy = new FormData();
    wUseProxy.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.UseProxyMails.Tooltip"));
    fdUseProxy.left = new FormAttachment(middle, 0);
    fdUseProxy.top = new FormAttachment(wlUseProxy, 0, SWT.CENTER);
    fdUseProxy.right = new FormAttachment(100, 0);
    wUseProxy.setLayoutData(fdUseProxy);

    wUseProxy.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            setUserProxy();
            action.setChanged();
          }
        });

    // ProxyUsername line
    wlProxyUsername = new Label(wServerSettings, SWT.RIGHT);
    wlProxyUsername.setText(BaseMessages.getString(PKG, "ActionGetPOP.ProxyUsername.Label"));
    PropsUi.setLook(wlProxyUsername);
    FormData fdlProxyUsername = new FormData();
    fdlProxyUsername.left = new FormAttachment(0, 0);
    fdlProxyUsername.top = new FormAttachment(wlUseProxy, 2 * margin);
    fdlProxyUsername.right = new FormAttachment(middle, -margin);
    wlProxyUsername.setLayoutData(fdlProxyUsername);
    wProxyUsername = new TextVar(variables, wServerSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wProxyUsername);
    wProxyUsername.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.ProxyUsername.Tooltip"));
    wProxyUsername.addModifyListener(lsMod);
    FormData fdProxyUsername = new FormData();
    fdProxyUsername.left = new FormAttachment(middle, 0);
    fdProxyUsername.top = new FormAttachment(wlProxyUsername, 0, SWT.CENTER);
    fdProxyUsername.right = new FormAttachment(100, 0);
    wProxyUsername.setLayoutData(fdProxyUsername);

    // Protocol
    Label wlProtocol = new Label(wServerSettings, SWT.RIGHT);
    wlProtocol.setText(BaseMessages.getString(PKG, "ActionGetPOP.Protocol.Label"));
    PropsUi.setLook(wlProtocol);
    FormData fdlProtocol = new FormData();
    fdlProtocol.left = new FormAttachment(0, 0);
    fdlProtocol.right = new FormAttachment(middle, -margin);
    fdlProtocol.top = new FormAttachment(wProxyUsername, margin);
    wlProtocol.setLayoutData(fdlProtocol);
    wProtocol = new CCombo(wServerSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
    wProtocol.setItems(MailConnectionMeta.protocolCodes);
    wProtocol.select(0);
    PropsUi.setLook(wProtocol);
    FormData fdProtocol = new FormData();
    fdProtocol.left = new FormAttachment(middle, 0);
    fdProtocol.top = new FormAttachment(wProxyUsername, margin);
    fdProtocol.right = new FormAttachment(100, 0);
    wProtocol.setLayoutData(fdProtocol);
    wProtocol.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            refreshProtocol(true);
          }
        });

    // Test connection button
    Button wTest = new Button(wServerSettings, SWT.PUSH);
    wTest.setText(BaseMessages.getString(PKG, "ActionGetPOP.TestConnection.Label"));
    PropsUi.setLook(wTest);
    FormData fdTest = new FormData();
    wTest.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.TestConnection.Tooltip"));
    fdTest.top = new FormAttachment(wProtocol, margin);
    fdTest.right = new FormAttachment(100, 0);
    wTest.setLayoutData(fdTest);
    wTest.addListener(SWT.Selection, e -> test());

    FormData fdServerSettings = new FormData();
    fdServerSettings.left = new FormAttachment(0, margin);
    fdServerSettings.top = new FormAttachment(wConnectionGroup, margin);
    fdServerSettings.right = new FormAttachment(100, -margin);
    wServerSettings.setLayoutData(fdServerSettings);
    // ///////////////////////////////////////////////////////////
    // / END OF SERVER SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF Target Folder GROUP///
    // /
    Group wTargetFolder = new Group(wGeneralComp, SWT.SHADOW_NONE);
    PropsUi.setLook(wTargetFolder);
    wTargetFolder.setText(BaseMessages.getString(PKG, "ActionGetPOP.TargetFolder.Group.Label"));

    FormLayout targetFoldergroupLayout = new FormLayout();
    targetFoldergroupLayout.marginWidth = 10;
    targetFoldergroupLayout.marginHeight = 10;
    wTargetFolder.setLayout(targetFoldergroupLayout);

    // OutputDirectory line
    wlOutputDirectory = new Label(wTargetFolder, SWT.RIGHT);
    wlOutputDirectory.setText(BaseMessages.getString(PKG, "ActionGetPOP.OutputDirectory.Label"));
    PropsUi.setLook(wlOutputDirectory);
    FormData fdlOutputDirectory = new FormData();
    fdlOutputDirectory.left = new FormAttachment(0, 0);
    fdlOutputDirectory.top = new FormAttachment(wServerSettings, margin);
    fdlOutputDirectory.right = new FormAttachment(middle, -margin);
    wlOutputDirectory.setLayoutData(fdlOutputDirectory);

    // Browse Source folders button ...
    wbDirectory = new Button(wTargetFolder, SWT.PUSH | SWT.CENTER);
    PropsUi.setLook(wbDirectory);
    wbDirectory.setText(BaseMessages.getString(PKG, "ActionGetPOP.BrowseFolders.Label"));
    FormData fdbDirectory = new FormData();
    fdbDirectory.right = new FormAttachment(100, -margin);
    fdbDirectory.top = new FormAttachment(wServerSettings, margin);
    wbDirectory.setLayoutData(fdbDirectory);
    wbDirectory.addListener(
        SWT.Selection, e -> BaseDialog.presentDirectoryDialog(shell, wOutputDirectory, variables));

    wOutputDirectory = new TextVar(variables, wTargetFolder, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wOutputDirectory);
    wOutputDirectory.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.OutputDirectory.Tooltip"));
    wOutputDirectory.addModifyListener(lsMod);
    FormData fdOutputDirectory = new FormData();
    fdOutputDirectory.left = new FormAttachment(middle, 0);
    fdOutputDirectory.top = new FormAttachment(wServerSettings, margin);
    fdOutputDirectory.right = new FormAttachment(wbDirectory, -margin);
    wOutputDirectory.setLayoutData(fdOutputDirectory);

    // Create local folder
    wlCreateLocalFolder = new Label(wTargetFolder, SWT.RIGHT);
    wlCreateLocalFolder.setText(
        BaseMessages.getString(PKG, "ActionGetPOP.createLocalFolder.Label"));
    PropsUi.setLook(wlCreateLocalFolder);
    FormData fdlCreateLocalFolder = new FormData();
    fdlCreateLocalFolder.left = new FormAttachment(0, 0);
    fdlCreateLocalFolder.top = new FormAttachment(wOutputDirectory, margin);
    fdlCreateLocalFolder.right = new FormAttachment(middle, -margin);
    wlCreateLocalFolder.setLayoutData(fdlCreateLocalFolder);
    wCreateLocalFolder = new Button(wTargetFolder, SWT.CHECK);
    PropsUi.setLook(wCreateLocalFolder);
    FormData fdCreateLocalFolder = new FormData();
    wCreateLocalFolder.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.createLocalFolder.Tooltip"));
    fdCreateLocalFolder.left = new FormAttachment(middle, 0);
    fdCreateLocalFolder.top = new FormAttachment(wlCreateLocalFolder, 0, SWT.CENTER);
    fdCreateLocalFolder.right = new FormAttachment(100, 0);
    wCreateLocalFolder.setLayoutData(fdCreateLocalFolder);

    // Filename pattern line
    wlFilenamePattern = new Label(wTargetFolder, SWT.RIGHT);
    wlFilenamePattern.setText(BaseMessages.getString(PKG, "ActionGetPOP.FilenamePattern.Label"));
    PropsUi.setLook(wlFilenamePattern);
    FormData fdlFilenamePattern = new FormData();
    fdlFilenamePattern.left = new FormAttachment(0, 0);
    fdlFilenamePattern.top = new FormAttachment(wlCreateLocalFolder, 2 * margin);
    fdlFilenamePattern.right = new FormAttachment(middle, -margin);
    wlFilenamePattern.setLayoutData(fdlFilenamePattern);
    wFilenamePattern = new TextVar(variables, wTargetFolder, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wFilenamePattern);
    wFilenamePattern.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.FilenamePattern.Tooltip"));
    wFilenamePattern.addModifyListener(lsMod);
    FormData fdFilenamePattern = new FormData();
    fdFilenamePattern.left = new FormAttachment(middle, 0);
    fdFilenamePattern.top = new FormAttachment(wlCreateLocalFolder, 2 * margin);
    fdFilenamePattern.right = new FormAttachment(100, 0);
    wFilenamePattern.setLayoutData(fdFilenamePattern);

    // Whenever something changes, set the tooltip to the expanded version:
    wFilenamePattern.addModifyListener(
        e -> wFilenamePattern.setToolTipText(variables.resolve(wFilenamePattern.getText())));

    // Get message?
    wlGetMessage = new Label(wTargetFolder, SWT.RIGHT);
    wlGetMessage.setText(BaseMessages.getString(PKG, "ActionGetPOP.GetMessageMails.Label"));
    PropsUi.setLook(wlGetMessage);
    FormData fdlGetMessage = new FormData();
    fdlGetMessage.left = new FormAttachment(0, 0);
    fdlGetMessage.top = new FormAttachment(wFilenamePattern, margin);
    fdlGetMessage.right = new FormAttachment(middle, -margin);
    wlGetMessage.setLayoutData(fdlGetMessage);
    wGetMessage = new Button(wTargetFolder, SWT.CHECK);
    PropsUi.setLook(wGetMessage);
    FormData fdGetMessage = new FormData();
    wGetMessage.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.GetMessageMails.Tooltip"));
    fdGetMessage.left = new FormAttachment(middle, 0);
    fdGetMessage.top = new FormAttachment(wlGetMessage, 0, SWT.CENTER);
    fdGetMessage.right = new FormAttachment(100, 0);
    wGetMessage.setLayoutData(fdGetMessage);

    wGetMessage.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            if (!wGetAttachment.getSelection() && !wGetMessage.getSelection()) {
              wGetAttachment.setSelection(true);
            }
          }
        });

    // Get attachment?
    wlGetAttachment = new Label(wTargetFolder, SWT.RIGHT);
    wlGetAttachment.setText(BaseMessages.getString(PKG, "ActionGetPOP.GetAttachmentMails.Label"));
    PropsUi.setLook(wlGetAttachment);
    FormData fdlGetAttachment = new FormData();
    fdlGetAttachment.left = new FormAttachment(0, 0);
    fdlGetAttachment.top = new FormAttachment(wlGetMessage, 2 * margin);
    fdlGetAttachment.right = new FormAttachment(middle, -margin);
    wlGetAttachment.setLayoutData(fdlGetAttachment);
    wGetAttachment = new Button(wTargetFolder, SWT.CHECK);
    PropsUi.setLook(wGetAttachment);
    FormData fdGetAttachment = new FormData();
    wGetAttachment.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.GetAttachmentMails.Tooltip"));
    fdGetAttachment.left = new FormAttachment(middle, 0);
    fdGetAttachment.top = new FormAttachment(wlGetAttachment, 0, SWT.CENTER);
    fdGetAttachment.right = new FormAttachment(100, 0);
    wGetAttachment.setLayoutData(fdGetAttachment);

    wGetAttachment.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            activeAttachmentFolder();
          }
        });

    // different folder for attachment?
    wlDifferentFolderForAttachment = new Label(wTargetFolder, SWT.RIGHT);
    wlDifferentFolderForAttachment.setText(
        BaseMessages.getString(PKG, "ActionGetPOP.DifferentFolderForAttachmentMails.Label"));
    PropsUi.setLook(wlDifferentFolderForAttachment);
    FormData fdlDifferentFolderForAttachment = new FormData();
    fdlDifferentFolderForAttachment.left = new FormAttachment(0, 0);
    fdlDifferentFolderForAttachment.top = new FormAttachment(wlGetAttachment, 2 * margin);
    fdlDifferentFolderForAttachment.right = new FormAttachment(middle, -margin);
    wlDifferentFolderForAttachment.setLayoutData(fdlDifferentFolderForAttachment);
    wDifferentFolderForAttachment = new Button(wTargetFolder, SWT.CHECK);
    PropsUi.setLook(wDifferentFolderForAttachment);
    FormData fdDifferentFolderForAttachment = new FormData();
    wDifferentFolderForAttachment.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.DifferentFolderForAttachmentMails.Tooltip"));
    fdDifferentFolderForAttachment.left = new FormAttachment(middle, 0);
    fdDifferentFolderForAttachment.top =
        new FormAttachment(wlDifferentFolderForAttachment, 0, SWT.CENTER);
    fdDifferentFolderForAttachment.right = new FormAttachment(100, 0);
    wDifferentFolderForAttachment.setLayoutData(fdDifferentFolderForAttachment);

    wDifferentFolderForAttachment.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            activeAttachmentFolder();
          }
        });

    // AttachmentFolder line
    wlAttachmentFolder = new Label(wTargetFolder, SWT.RIGHT);
    wlAttachmentFolder.setText(BaseMessages.getString(PKG, "ActionGetPOP.AttachmentFolder.Label"));
    PropsUi.setLook(wlAttachmentFolder);
    FormData fdlAttachmentFolder = new FormData();
    fdlAttachmentFolder.left = new FormAttachment(0, 0);
    fdlAttachmentFolder.top = new FormAttachment(wlDifferentFolderForAttachment, 2 * margin);
    fdlAttachmentFolder.right = new FormAttachment(middle, -margin);
    wlAttachmentFolder.setLayoutData(fdlAttachmentFolder);

    // Browse Source folders button ...
    wbAttachmentFolder = new Button(wTargetFolder, SWT.PUSH | SWT.CENTER);
    PropsUi.setLook(wbAttachmentFolder);
    wbAttachmentFolder.setText(BaseMessages.getString(PKG, "ActionGetPOP.BrowseFolders.Label"));
    FormData fdbAttachmentFolder = new FormData();
    fdbAttachmentFolder.right = new FormAttachment(100, -margin);
    fdbAttachmentFolder.top = new FormAttachment(wlAttachmentFolder, 0, SWT.CENTER);
    wbAttachmentFolder.setLayoutData(fdbAttachmentFolder);
    wbAttachmentFolder.addListener(
        SWT.Selection, e -> BaseDialog.presentDirectoryDialog(shell, wAttachmentFolder, variables));

    wAttachmentFolder = new TextVar(variables, wTargetFolder, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wAttachmentFolder);
    wAttachmentFolder.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.AttachmentFolder.Tooltip"));
    wAttachmentFolder.addModifyListener(lsMod);
    FormData fdAttachmentFolder = new FormData();
    fdAttachmentFolder.left = new FormAttachment(middle, 0);
    fdAttachmentFolder.top = new FormAttachment(wlAttachmentFolder, 0, SWT.CENTER);
    fdAttachmentFolder.right = new FormAttachment(wbAttachmentFolder, -margin);
    wAttachmentFolder.setLayoutData(fdAttachmentFolder);

    // Limit attached files
    wlAttachmentWildcard = new Label(wTargetFolder, SWT.RIGHT);
    wlAttachmentWildcard.setText(
        BaseMessages.getString(PKG, "ActionGetPOP.AttachmentWildcard.Label"));
    PropsUi.setLook(wlAttachmentWildcard);
    FormData fdlAttachmentWildcard = new FormData();
    fdlAttachmentWildcard.left = new FormAttachment(0, 0);
    fdlAttachmentWildcard.top = new FormAttachment(wbAttachmentFolder, margin);
    fdlAttachmentWildcard.right = new FormAttachment(middle, -margin);
    wlAttachmentWildcard.setLayoutData(fdlAttachmentWildcard);
    wAttachmentWildcard = new TextVar(variables, wTargetFolder, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wAttachmentWildcard);
    wAttachmentWildcard.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.AttachmentWildcard.Tooltip"));
    wAttachmentWildcard.addModifyListener(lsMod);
    FormData fdAttachmentWildcard = new FormData();
    fdAttachmentWildcard.left = new FormAttachment(middle, 0);
    fdAttachmentWildcard.top = new FormAttachment(wbAttachmentFolder, margin);
    fdAttachmentWildcard.right = new FormAttachment(100, 0);
    wAttachmentWildcard.setLayoutData(fdAttachmentWildcard);

    // Whenever something changes, set the tooltip to the expanded version:
    wAttachmentWildcard.addModifyListener(
        e -> wAttachmentWildcard.setToolTipText(variables.resolve(wAttachmentWildcard.getText())));

    FormData fdTargetFolder = new FormData();
    fdTargetFolder.left = new FormAttachment(0, margin);
    fdTargetFolder.top = new FormAttachment(wServerSettings, margin);
    fdTargetFolder.right = new FormAttachment(100, -margin);
    wTargetFolder.setLayoutData(fdTargetFolder);
    // ///////////////////////////////////////////////////////////
    // / END OF SERVER SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    FormData fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment(0, 0);
    fdGeneralComp.top = new FormAttachment(wName, 0);
    fdGeneralComp.right = new FormAttachment(100, 0);
    fdGeneralComp.bottom = new FormAttachment(100, 0);
    wGeneralComp.setLayoutData(fdGeneralComp);

    wGeneralComp.layout();
    wGeneralTab.setControl(wGeneralComp);
    PropsUi.setLook(wGeneralComp);

    // ///////////////////////////////////////////////////////////
    // / END OF GENERAL TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF SETTINGS TAB ///
    // ////////////////////////

    CTabItem wSettingsTab = new CTabItem(wTabFolder, SWT.NONE);
    wSettingsTab.setFont(GuiResource.getInstance().getFontDefault());
    wSettingsTab.setText(BaseMessages.getString(PKG, "ActionGetPOP.Tab.Pop.Label"));
    Composite wSettingsComp = new Composite(wTabFolder, SWT.NONE);
    PropsUi.setLook(wSettingsComp);
    FormLayout popLayout = new FormLayout();
    popLayout.marginWidth = 3;
    popLayout.marginHeight = 3;
    wSettingsComp.setLayout(popLayout);

    // Action type
    Label wlActionType = new Label(wSettingsComp, SWT.RIGHT);
    wlActionType.setText(BaseMessages.getString(PKG, "ActionGetPOP.ActionType.Label"));
    PropsUi.setLook(wlActionType);
    FormData fdlActionType = new FormData();
    fdlActionType.left = new FormAttachment(0, 0);
    fdlActionType.right = new FormAttachment(middle, -margin);
    fdlActionType.top = new FormAttachment(0, 3 * margin);
    wlActionType.setLayoutData(fdlActionType);

    wActionType = new CCombo(wSettingsComp, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
    wActionType.setItems(MailConnectionMeta.actionTypeDesc);
    wActionType.select(0); // +1: starts at -1

    PropsUi.setLook(wActionType);
    FormData fdActionType = new FormData();
    fdActionType.left = new FormAttachment(middle, 0);
    fdActionType.top = new FormAttachment(0, 3 * margin);
    fdActionType.right = new FormAttachment(100, 0);
    wActionType.setLayoutData(fdActionType);
    wActionType.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            setActionType();
            action.setChanged();
          }
        });

    // Message: for POP3, only INBOX folder is available!
    wlPOP3Message = new Label(wSettingsComp, SWT.RIGHT);
    wlPOP3Message.setText(BaseMessages.getString(PKG, "ActionGetPOP.POP3Message.Label"));
    PropsUi.setLook(wlPOP3Message);
    FormData fdlPOP3Message = new FormData();
    fdlPOP3Message.left = new FormAttachment(0, margin);
    fdlPOP3Message.top = new FormAttachment(wActionType, 3 * margin);
    wlPOP3Message.setLayoutData(fdlPOP3Message);
    wlPOP3Message.setForeground(GuiResource.getInstance().getColorOrange());

    // ////////////////////////
    // START OF POP3 Settings GROUP///
    // /
    Group wPOP3Settings = new Group(wSettingsComp, SWT.SHADOW_NONE);
    PropsUi.setLook(wPOP3Settings);
    wPOP3Settings.setText(BaseMessages.getString(PKG, "ActionGetPOP.POP3Settings.Group.Label"));

    FormLayout pop3Settingsgrouplayout = new FormLayout();
    pop3Settingsgrouplayout.marginWidth = 10;
    pop3Settingsgrouplayout.marginHeight = 10;
    wPOP3Settings.setLayout(pop3Settingsgrouplayout);

    // List of mails of retrieve
    wlListmails = new Label(wPOP3Settings, SWT.RIGHT);
    wlListmails.setText(BaseMessages.getString(PKG, "ActionGetPOP.Listmails.Label"));
    PropsUi.setLook(wlListmails);
    FormData fdlListmails = new FormData();
    fdlListmails.left = new FormAttachment(0, 0);
    fdlListmails.right = new FormAttachment(middle, 0);
    fdlListmails.top = new FormAttachment(wlPOP3Message, 2 * margin);
    wlListmails.setLayoutData(fdlListmails);
    wListmails = new CCombo(wPOP3Settings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
    wListmails.add(BaseMessages.getString(PKG, "ActionGetPOP.RetrieveAllMails.Label"));
    wListmails.add(BaseMessages.getString(PKG, "ActionGetPOP.RetrieveFirstMails.Label"));
    wListmails.select(0); // +1: starts at -1

    PropsUi.setLook(wListmails);
    FormData fdListmails = new FormData();
    fdListmails.left = new FormAttachment(middle, 0);
    fdListmails.top = new FormAttachment(wlPOP3Message, 2 * margin);
    fdListmails.right = new FormAttachment(100, 0);
    wListmails.setLayoutData(fdListmails);

    wListmails.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            action.setChanged();
            chooseListMails();
          }
        });

    // Retrieve the first ... mails
    wlFirstmails = new Label(wPOP3Settings, SWT.RIGHT);
    wlFirstmails.setText(BaseMessages.getString(PKG, "ActionGetPOP.Firstmails.Label"));
    PropsUi.setLook(wlFirstmails);
    FormData fdlFirstmails = new FormData();
    fdlFirstmails.left = new FormAttachment(0, 0);
    fdlFirstmails.right = new FormAttachment(middle, -margin);
    fdlFirstmails.top = new FormAttachment(wListmails, margin);
    wlFirstmails.setLayoutData(fdlFirstmails);

    wFirstmails = new TextVar(variables, wPOP3Settings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wFirstmails);
    wFirstmails.addModifyListener(lsMod);
    FormData fdFirstmails = new FormData();
    fdFirstmails.left = new FormAttachment(middle, 0);
    fdFirstmails.top = new FormAttachment(wListmails, margin);
    fdFirstmails.right = new FormAttachment(100, 0);
    wFirstmails.setLayoutData(fdFirstmails);

    // Delete mails after retrieval...
    wlDelete = new Label(wPOP3Settings, SWT.RIGHT);
    wlDelete.setText(BaseMessages.getString(PKG, "ActionGetPOP.DeleteMails.Label"));
    PropsUi.setLook(wlDelete);
    FormData fdlDelete = new FormData();
    fdlDelete.left = new FormAttachment(0, 0);
    fdlDelete.top = new FormAttachment(wFirstmails, margin);
    fdlDelete.right = new FormAttachment(middle, -margin);
    wlDelete.setLayoutData(fdlDelete);
    wDelete = new Button(wPOP3Settings, SWT.CHECK);
    PropsUi.setLook(wDelete);
    FormData fdDelete = new FormData();
    wDelete.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.DeleteMails.Tooltip"));
    fdDelete.left = new FormAttachment(middle, 0);
    fdDelete.top = new FormAttachment(wlDelete, 0, SWT.CENTER);
    fdDelete.right = new FormAttachment(100, 0);
    wDelete.setLayoutData(fdDelete);

    FormData fdPOP3Settings = new FormData();
    fdPOP3Settings.left = new FormAttachment(0, margin);
    fdPOP3Settings.top = new FormAttachment(wlPOP3Message, 2 * margin);
    fdPOP3Settings.right = new FormAttachment(100, -margin);
    wPOP3Settings.setLayoutData(fdPOP3Settings);
    // ///////////////////////////////////////////////////////////
    // / END OF POP3 SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF IMAP Settings GROUP///
    // /
    Group wIMAPSettings = new Group(wSettingsComp, SWT.SHADOW_NONE);
    PropsUi.setLook(wIMAPSettings);
    wIMAPSettings.setText(BaseMessages.getString(PKG, "ActionGetPOP.IMAPSettings.Groupp.Label"));

    FormLayout imapSettingsgroupLayout = new FormLayout();
    imapSettingsgroupLayout.marginWidth = 10;
    imapSettingsgroupLayout.marginHeight = 10;
    wIMAPSettings.setLayout(imapSettingsgroupLayout);

    // SelectFolder button
    wSelectFolder = new Button(wIMAPSettings, SWT.PUSH);
    wSelectFolder.setText(BaseMessages.getString(PKG, "ActionGetPOP.SelectFolderConnection.Label"));
    PropsUi.setLook(wSelectFolder);
    FormData fdSelectFolder = new FormData();
    wSelectFolder.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.SelectFolderConnection.Tooltip"));
    fdSelectFolder.top = new FormAttachment(wPOP3Settings, margin);
    fdSelectFolder.right = new FormAttachment(100, 0);
    wSelectFolder.setLayoutData(fdSelectFolder);
    wSelectFolder.addListener(SWT.Selection, e -> selectFolder(wIMAPFolder));

    // TestIMAPFolder button
    wTestIMAPFolder = new Button(wIMAPSettings, SWT.PUSH);
    wTestIMAPFolder.setText(
        BaseMessages.getString(PKG, "ActionGetPOP.TestIMAPFolderConnection.Label"));
    PropsUi.setLook(wTestIMAPFolder);
    FormData fdTestIMAPFolder = new FormData();
    wTestIMAPFolder.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.TestIMAPFolderConnection.Tooltip"));
    fdTestIMAPFolder.top = new FormAttachment(wPOP3Settings, margin);
    fdTestIMAPFolder.right = new FormAttachment(wSelectFolder, -margin);
    wTestIMAPFolder.setLayoutData(fdTestIMAPFolder);
    wTestIMAPFolder.addListener(
        SWT.Selection, e -> checkFolder(variables.resolve(wIMAPFolder.getText())));

    // IMAPFolder line
    wlIMAPFolder = new Label(wIMAPSettings, SWT.RIGHT);
    wlIMAPFolder.setText(BaseMessages.getString(PKG, "ActionGetPOP.IMAPFolder.Label"));
    PropsUi.setLook(wlIMAPFolder);
    FormData fdlIMAPFolder = new FormData();
    fdlIMAPFolder.left = new FormAttachment(0, 0);
    fdlIMAPFolder.top = new FormAttachment(wPOP3Settings, margin);
    fdlIMAPFolder.right = new FormAttachment(middle, -margin);
    wlIMAPFolder.setLayoutData(fdlIMAPFolder);
    wIMAPFolder = new TextVar(variables, wIMAPSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wIMAPFolder);
    wIMAPFolder.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.IMAPFolder.Tooltip"));
    wIMAPFolder.addModifyListener(lsMod);
    FormData fdIMAPFolder = new FormData();
    fdIMAPFolder.left = new FormAttachment(middle, 0);
    fdIMAPFolder.top = new FormAttachment(wPOP3Settings, margin);
    fdIMAPFolder.right = new FormAttachment(wTestIMAPFolder, -margin);
    wIMAPFolder.setLayoutData(fdIMAPFolder);

    // Include subfolders?
    wlIncludeSubFolders = new Label(wIMAPSettings, SWT.RIGHT);
    wlIncludeSubFolders.setText(
        BaseMessages.getString(PKG, "ActionGetPOP.IncludeSubFoldersMails.Label"));
    PropsUi.setLook(wlIncludeSubFolders);
    FormData fdlIncludeSubFolders = new FormData();
    fdlIncludeSubFolders.left = new FormAttachment(0, 0);
    fdlIncludeSubFolders.top = new FormAttachment(wIMAPFolder, margin);
    fdlIncludeSubFolders.right = new FormAttachment(middle, -margin);
    wlIncludeSubFolders.setLayoutData(fdlIncludeSubFolders);
    wIncludeSubFolders = new Button(wIMAPSettings, SWT.CHECK);
    PropsUi.setLook(wIncludeSubFolders);
    FormData fdIncludeSubFolders = new FormData();
    wIncludeSubFolders.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.IncludeSubFoldersMails.Tooltip"));
    fdIncludeSubFolders.left = new FormAttachment(middle, 0);
    fdIncludeSubFolders.top = new FormAttachment(wlIncludeSubFolders, 0, SWT.CENTER);
    fdIncludeSubFolders.right = new FormAttachment(100, 0);
    wIncludeSubFolders.setLayoutData(fdIncludeSubFolders);
    wIncludeSubFolders.addSelectionListener(lsSelection);

    // List of mails of retrieve
    wlIMAPListmails = new Label(wIMAPSettings, SWT.RIGHT);
    wlIMAPListmails.setText(BaseMessages.getString(PKG, "ActionGetPOP.IMAPListmails.Label"));
    PropsUi.setLook(wlIMAPListmails);
    FormData fdlIMAPListmails = new FormData();
    fdlIMAPListmails.left = new FormAttachment(0, 0);
    fdlIMAPListmails.right = new FormAttachment(middle, -margin);
    fdlIMAPListmails.top = new FormAttachment(wlIncludeSubFolders, 2 * margin);
    wlIMAPListmails.setLayoutData(fdlIMAPListmails);
    wIMAPListmails = new CCombo(wIMAPSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
    wIMAPListmails.setItems(MailConnectionMeta.valueIMAPListDesc);
    wIMAPListmails.select(0); // +1: starts at -1
    PropsUi.setLook(wIMAPListmails);
    FormData fdIMAPListmails = new FormData();
    fdIMAPListmails.left = new FormAttachment(middle, 0);
    fdIMAPListmails.top = new FormAttachment(wlIncludeSubFolders, 2 * margin);
    fdIMAPListmails.right = new FormAttachment(100, 0);
    wIMAPListmails.setLayoutData(fdIMAPListmails);

    wIMAPListmails.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            // disalbe selection event

          }
        });

    // Retrieve the first ... mails
    wlIMAPFirstmails = new Label(wIMAPSettings, SWT.RIGHT);
    wlIMAPFirstmails.setText(BaseMessages.getString(PKG, "ActionGetPOP.IMAPFirstmails.Label"));
    PropsUi.setLook(wlIMAPFirstmails);
    FormData fdlIMAPFirstmails = new FormData();
    fdlIMAPFirstmails.left = new FormAttachment(0, 0);
    fdlIMAPFirstmails.right = new FormAttachment(middle, -margin);
    fdlIMAPFirstmails.top = new FormAttachment(wIMAPListmails, margin);
    wlIMAPFirstmails.setLayoutData(fdlIMAPFirstmails);

    wIMAPFirstmails = new TextVar(variables, wIMAPSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wIMAPFirstmails);
    wIMAPFirstmails.addModifyListener(lsMod);
    FormData fdIMAPFirstmails = new FormData();
    fdIMAPFirstmails.left = new FormAttachment(middle, 0);
    fdIMAPFirstmails.top = new FormAttachment(wIMAPListmails, margin);
    fdIMAPFirstmails.right = new FormAttachment(100, 0);
    wIMAPFirstmails.setLayoutData(fdIMAPFirstmails);

    // After get IMAP
    wlAfterGetIMAP = new Label(wIMAPSettings, SWT.RIGHT);
    wlAfterGetIMAP.setText(BaseMessages.getString(PKG, "ActionGetPOP.AfterGetIMAP.Label"));
    PropsUi.setLook(wlAfterGetIMAP);
    FormData fdlAfterGetIMAP = new FormData();
    fdlAfterGetIMAP.left = new FormAttachment(0, 0);
    fdlAfterGetIMAP.right = new FormAttachment(middle, -margin);
    fdlAfterGetIMAP.top = new FormAttachment(wIMAPFirstmails, 2 * margin);
    wlAfterGetIMAP.setLayoutData(fdlAfterGetIMAP);
    wAfterGetIMAP = new CCombo(wIMAPSettings, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
    wAfterGetIMAP.setItems(MailConnectionMeta.afterGetIMAPDesc);
    wAfterGetIMAP.select(0); // +1: starts at -1

    PropsUi.setLook(wAfterGetIMAP);
    FormData fdAfterGetIMAP = new FormData();
    fdAfterGetIMAP.left = new FormAttachment(middle, 0);
    fdAfterGetIMAP.top = new FormAttachment(wIMAPFirstmails, 2 * margin);
    fdAfterGetIMAP.right = new FormAttachment(100, 0);
    wAfterGetIMAP.setLayoutData(fdAfterGetIMAP);

    wAfterGetIMAP.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            setAfterIMAPRetrived();
            action.setChanged();
          }
        });

    // MoveToFolder line
    wlMoveToFolder = new Label(wIMAPSettings, SWT.RIGHT);
    wlMoveToFolder.setText(BaseMessages.getString(PKG, "ActionGetPOP.MoveToFolder.Label"));
    PropsUi.setLook(wlMoveToFolder);
    FormData fdlMoveToFolder = new FormData();
    fdlMoveToFolder.left = new FormAttachment(0, 0);
    fdlMoveToFolder.top = new FormAttachment(wAfterGetIMAP, margin);
    fdlMoveToFolder.right = new FormAttachment(middle, -margin);
    wlMoveToFolder.setLayoutData(fdlMoveToFolder);

    // SelectMoveToFolder button
    wSelectMoveToFolder = new Button(wIMAPSettings, SWT.PUSH);
    wSelectMoveToFolder.setText(
        BaseMessages.getString(PKG, "ActionGetPOP.SelectMoveToFolderConnection.Label"));
    PropsUi.setLook(wSelectMoveToFolder);
    FormData fdSelectMoveToFolder = new FormData();
    wSelectMoveToFolder.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.SelectMoveToFolderConnection.Tooltip"));
    fdSelectMoveToFolder.top = new FormAttachment(wAfterGetIMAP, margin);
    fdSelectMoveToFolder.right = new FormAttachment(100, 0);
    wSelectMoveToFolder.setLayoutData(fdSelectMoveToFolder);
    wSelectMoveToFolder.addListener(SWT.Selection, e -> selectFolder(wMoveToFolder));

    // TestMoveToFolder button
    wTestMoveToFolder = new Button(wIMAPSettings, SWT.PUSH);
    wTestMoveToFolder.setText(
        BaseMessages.getString(PKG, "ActionGetPOP.TestMoveToFolderConnection.Label"));
    PropsUi.setLook(wTestMoveToFolder);
    FormData fdTestMoveToFolder = new FormData();
    wTestMoveToFolder.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.TestMoveToFolderConnection.Tooltip"));
    fdTestMoveToFolder.top = new FormAttachment(wAfterGetIMAP, margin);
    fdTestMoveToFolder.right = new FormAttachment(wSelectMoveToFolder, -margin);
    wTestMoveToFolder.setLayoutData(fdTestMoveToFolder);
    wTestMoveToFolder.addListener(
        SWT.Selection, e -> checkFolder(variables.resolve(wMoveToFolder.getText())));

    wMoveToFolder = new TextVar(variables, wIMAPSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wMoveToFolder);
    wMoveToFolder.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.MoveToFolder.Tooltip"));
    wMoveToFolder.addModifyListener(lsMod);
    FormData fdMoveToFolder = new FormData();
    fdMoveToFolder.left = new FormAttachment(middle, 0);
    fdMoveToFolder.top = new FormAttachment(wAfterGetIMAP, margin);
    fdMoveToFolder.right = new FormAttachment(wTestMoveToFolder, -margin);
    wMoveToFolder.setLayoutData(fdMoveToFolder);

    // Create move to folder
    wlCreateMoveToFolder = new Label(wIMAPSettings, SWT.RIGHT);
    wlCreateMoveToFolder.setText(
        BaseMessages.getString(PKG, "ActionGetPOP.createMoveToFolderMails.Label"));
    PropsUi.setLook(wlCreateMoveToFolder);
    FormData fdlCreateMoveToFolder = new FormData();
    fdlCreateMoveToFolder.left = new FormAttachment(0, 0);
    fdlCreateMoveToFolder.top = new FormAttachment(wMoveToFolder, margin);
    fdlCreateMoveToFolder.right = new FormAttachment(middle, -margin);
    wlCreateMoveToFolder.setLayoutData(fdlCreateMoveToFolder);
    wCreateMoveToFolder = new Button(wIMAPSettings, SWT.CHECK);
    PropsUi.setLook(wCreateMoveToFolder);
    FormData fdCreateMoveToFolder = new FormData();
    wCreateMoveToFolder.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.createMoveToFolderMails.Tooltip"));
    fdCreateMoveToFolder.left = new FormAttachment(middle, 0);
    fdCreateMoveToFolder.top = new FormAttachment(wlCreateMoveToFolder, 0, SWT.CENTER);
    fdCreateMoveToFolder.right = new FormAttachment(100, 0);
    wCreateMoveToFolder.setLayoutData(fdCreateMoveToFolder);

    FormData fdIMAPSettings = new FormData();
    fdIMAPSettings.left = new FormAttachment(0, margin);
    fdIMAPSettings.top = new FormAttachment(wPOP3Settings, 2 * margin);
    fdIMAPSettings.right = new FormAttachment(100, -margin);
    wIMAPSettings.setLayoutData(fdIMAPSettings);
    // ///////////////////////////////////////////////////////////
    // / END OF IMAP SETTINGS GROUP
    // ///////////////////////////////////////////////////////////

    FormData fdSettingsComp = new FormData();
    fdSettingsComp.left = new FormAttachment(0, 0);
    fdSettingsComp.top = new FormAttachment(wName, 0);
    fdSettingsComp.right = new FormAttachment(100, 0);
    fdSettingsComp.bottom = new FormAttachment(100, 0);
    wSettingsComp.setLayoutData(fdSettingsComp);

    wSettingsComp.layout();
    wSettingsTab.setControl(wSettingsComp);
    PropsUi.setLook(wSettingsComp);

    // ///////////////////////////////////////////////////////////
    // / END OF Pop TAB
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF SEARCH TAB ///
    // ////////////////////////

    CTabItem wSearchTab = new CTabItem(wTabFolder, SWT.NONE);
    wSearchTab.setFont(GuiResource.getInstance().getFontDefault());
    wSearchTab.setText(BaseMessages.getString(PKG, "ActionGetPOP.Tab.Search.Label"));
    Composite wSearchComp = new Composite(wTabFolder, SWT.NONE);
    PropsUi.setLook(wSearchComp);
    FormLayout searchLayout = new FormLayout();
    searchLayout.marginWidth = 3;
    searchLayout.marginHeight = 3;
    wSearchComp.setLayout(searchLayout);

    // ////////////////////////
    // START OF HEADER ROUP///
    // /
    Group wHeader = new Group(wSearchComp, SWT.SHADOW_NONE);
    PropsUi.setLook(wHeader);
    wHeader.setText(BaseMessages.getString(PKG, "ActionGetPOP.Header.Group.Label"));

    FormLayout headergroupLayout = new FormLayout();
    headergroupLayout.marginWidth = 10;
    headergroupLayout.marginHeight = 10;
    wHeader.setLayout(headergroupLayout);

    // Sender line: label, text, negate checkbox
    //
    Label wlSender = new Label(wHeader, SWT.RIGHT);
    wlSender.setText(BaseMessages.getString(PKG, "ActionGetPOP.wSender.Label"));
    PropsUi.setLook(wlSender);
    FormData fdlSender = new FormData();
    fdlSender.left = new FormAttachment(0, 0);
    fdlSender.top = new FormAttachment(0, margin);
    fdlSender.right = new FormAttachment(middle, -margin);
    wlSender.setLayoutData(fdlSender);

    wNegateSender = new Button(wHeader, SWT.CHECK);
    PropsUi.setLook(wNegateSender);
    FormData fdNegateSender = new FormData();
    wNegateSender.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.NegateSender.Tooltip"));
    fdNegateSender.top = new FormAttachment(wlSender, 0, SWT.CENTER);
    fdNegateSender.right = new FormAttachment(100, -margin);
    wNegateSender.setLayoutData(fdNegateSender);

    wSender = new TextVar(variables, wHeader, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wSender);
    wSender.addModifyListener(lsMod);
    FormData fdSender = new FormData();
    fdSender.left = new FormAttachment(middle, 0);
    fdSender.top = new FormAttachment(wlSender, 0, SWT.CENTER);
    fdSender.right = new FormAttachment(wNegateSender, -margin);
    wSender.setLayoutData(fdSender);

    // Recipient: label, text, negate button
    //
    Label wlRecipient = new Label(wHeader, SWT.RIGHT);
    wlRecipient.setText(BaseMessages.getString(PKG, "ActionGetPOP.Receipient.Label"));
    PropsUi.setLook(wlRecipient);
    FormData fdlRecipient = new FormData();
    fdlRecipient.left = new FormAttachment(0, 0);
    fdlRecipient.top = new FormAttachment(wSender, 2 * margin);
    fdlRecipient.right = new FormAttachment(middle, -margin);
    wlRecipient.setLayoutData(fdlRecipient);

    wNegateReceipient = new Button(wHeader, SWT.CHECK);
    PropsUi.setLook(wNegateReceipient);
    FormData fdNegateRecipient = new FormData();
    wNegateReceipient.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.NegateReceipient.Tooltip"));
    fdNegateRecipient.top = new FormAttachment(wlRecipient, 0, SWT.CENTER);
    fdNegateRecipient.right = new FormAttachment(100, -margin);
    wNegateReceipient.setLayoutData(fdNegateRecipient);

    wRecipient = new TextVar(variables, wHeader, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wRecipient);
    wRecipient.addModifyListener(lsMod);
    FormData fdRecipient = new FormData();
    fdRecipient.left = new FormAttachment(middle, 0);
    fdRecipient.top = new FormAttachment(wlRecipient, 0, SWT.CENTER);
    fdRecipient.right = new FormAttachment(wNegateReceipient, -margin);
    wRecipient.setLayoutData(fdRecipient);

    // Subject line: label, text, negate checkbox
    //
    Label wlSubject = new Label(wHeader, SWT.RIGHT);
    wlSubject.setText(BaseMessages.getString(PKG, "ActionGetPOP.Subject.Label"));
    PropsUi.setLook(wlSubject);
    FormData fdlSubject = new FormData();
    fdlSubject.left = new FormAttachment(0, 0);
    fdlSubject.top = new FormAttachment(wRecipient, 2 * margin);
    fdlSubject.right = new FormAttachment(middle, -margin);
    wlSubject.setLayoutData(fdlSubject);

    wNegateSubject = new Button(wHeader, SWT.CHECK);
    PropsUi.setLook(wNegateSubject);
    FormData fdNegateSubject = new FormData();
    wNegateSubject.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.NegateSubject.Tooltip"));
    fdNegateSubject.top = new FormAttachment(wlSubject, 0, SWT.CENTER);
    fdNegateSubject.right = new FormAttachment(100, -margin);
    wNegateSubject.setLayoutData(fdNegateSubject);

    wSubject = new TextVar(variables, wHeader, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wSubject);
    wSubject.addModifyListener(lsMod);
    FormData fdSubject = new FormData();
    fdSubject.left = new FormAttachment(middle, 0);
    fdSubject.top = new FormAttachment(wlSubject, 0, SWT.CENTER);
    fdSubject.right = new FormAttachment(wNegateSubject, -margin);
    wSubject.setLayoutData(fdSubject);

    FormData fdHeader = new FormData();
    fdHeader.left = new FormAttachment(0, margin);
    fdHeader.top = new FormAttachment(wRecipient, 2 * margin);
    fdHeader.right = new FormAttachment(100, -margin);
    wHeader.setLayoutData(fdHeader);
    // ///////////////////////////////////////////////////////////
    // / END OF HEADER GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF CONTENT GROUP///
    // /
    Group wContent = new Group(wSearchComp, SWT.SHADOW_NONE);
    PropsUi.setLook(wContent);
    wContent.setText(BaseMessages.getString(PKG, "ActionGetPOP.Content.Group.Label"));

    FormLayout contentgroupLayout = new FormLayout();
    contentgroupLayout.marginWidth = 10;
    contentgroupLayout.marginHeight = 10;
    wContent.setLayout(contentgroupLayout);

    // Body: label, text, negate checkbox
    //
    Label wlBody = new Label(wContent, SWT.RIGHT);
    wlBody.setText(BaseMessages.getString(PKG, "ActionGetPOP.Body.Label"));
    PropsUi.setLook(wlBody);
    FormData fdlBody = new FormData();
    fdlBody.left = new FormAttachment(0, 0);
    fdlBody.top = new FormAttachment(0, margin);
    fdlBody.right = new FormAttachment(middle, -margin);
    wlBody.setLayoutData(fdlBody);

    wNegateBody = new Button(wContent, SWT.CHECK);
    PropsUi.setLook(wNegateBody);
    FormData fdNegateBody = new FormData();
    wNegateBody.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.NegateBody.Tooltip"));
    fdNegateBody.top = new FormAttachment(wlBody, 0, SWT.CENTER);
    fdNegateBody.right = new FormAttachment(100, -margin);
    wNegateBody.setLayoutData(fdNegateBody);

    wBody = new TextVar(variables, wContent, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    PropsUi.setLook(wBody);
    wBody.addModifyListener(lsMod);
    FormData fdBody = new FormData();
    fdBody.left = new FormAttachment(middle, 0);
    fdBody.top = new FormAttachment(wlBody, 0, SWT.CENTER);
    fdBody.right = new FormAttachment(wNegateBody, -margin);
    wBody.setLayoutData(fdBody);

    FormData fdContent = new FormData();
    fdContent.left = new FormAttachment(0, margin);
    fdContent.top = new FormAttachment(wHeader, margin);
    fdContent.right = new FormAttachment(100, -margin);
    wContent.setLayoutData(fdContent);
    // ///////////////////////////////////////////////////////////
    // / END OF CONTENT GROUP
    // ///////////////////////////////////////////////////////////

    // ////////////////////////
    // START OF RECEIVED DATE ROUP///
    // /
    Group wReceivedDate = new Group(wSearchComp, SWT.SHADOW_NONE);
    PropsUi.setLook(wReceivedDate);
    wReceivedDate.setText(BaseMessages.getString(PKG, "ActionGetPOP.ReceivedDate.Group.Label"));

    FormLayout receivedDategroupLayout = new FormLayout();
    receivedDategroupLayout.marginWidth = 10;
    receivedDategroupLayout.marginHeight = 10;
    wReceivedDate.setLayout(receivedDategroupLayout);

    // Received Date Condition line: label, text, negate checkbox
    //
    wlConditionOnReceivedDate = new Label(wReceivedDate, SWT.RIGHT);
    wlConditionOnReceivedDate.setText(
        BaseMessages.getString(PKG, "ActionGetPOP.ConditionOnReceivedDate.Label"));
    PropsUi.setLook(wlConditionOnReceivedDate);
    FormData fdlConditionOnReceivedDate = new FormData();
    fdlConditionOnReceivedDate.left = new FormAttachment(0, 0);
    fdlConditionOnReceivedDate.right = new FormAttachment(middle, -margin);
    fdlConditionOnReceivedDate.top = new FormAttachment(0, margin);
    wlConditionOnReceivedDate.setLayoutData(fdlConditionOnReceivedDate);

    wNegateReceivedDate = new Button(wReceivedDate, SWT.CHECK);
    PropsUi.setLook(wNegateReceivedDate);
    FormData fdNegateReceivedDate = new FormData();
    wNegateReceivedDate.setToolTipText(
        BaseMessages.getString(PKG, "ActionGetPOP.NegateReceivedDate.Tooltip"));
    fdNegateReceivedDate.top = new FormAttachment(wlConditionOnReceivedDate, 0, SWT.CENTER);
    fdNegateReceivedDate.right = new FormAttachment(100, -margin);
    wNegateReceivedDate.setLayoutData(fdNegateReceivedDate);

    wConditionOnReceivedDate = new CCombo(wReceivedDate, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
    wConditionOnReceivedDate.setItems(MailConnectionMeta.conditionDateDesc);
    wConditionOnReceivedDate.select(0); // +1: starts at -1
    PropsUi.setLook(wConditionOnReceivedDate);
    FormData fdConditionOnReceivedDate = new FormData();
    fdConditionOnReceivedDate.left = new FormAttachment(middle, 0);
    fdConditionOnReceivedDate.top = new FormAttachment(wlConditionOnReceivedDate, 0, SWT.CENTER);
    fdConditionOnReceivedDate.right = new FormAttachment(wNegateReceivedDate, -margin);
    wConditionOnReceivedDate.setLayoutData(fdConditionOnReceivedDate);
    wConditionOnReceivedDate.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            conditionReceivedDate();
            action.setChanged();
          }
        });

    open = new Button(wReceivedDate, SWT.PUSH);
    open.setImage(GuiResource.getInstance().getImageCalendar());
    open.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.OpenCalendar"));
    FormData fdlButton = new FormData();
    fdlButton.top = new FormAttachment(wConditionOnReceivedDate, margin);
    fdlButton.right = new FormAttachment(100, 0);
    open.setLayoutData(fdlButton);
    open.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM);
            dialog.setText(BaseMessages.getString(PKG, "ActionGetPOP.SelectDate"));
            dialog.setImage(GuiResource.getInstance().getImageHopUi());
            dialog.setLayout(new GridLayout(3, false));

            final DateTime calendar = new DateTime(dialog, SWT.CALENDAR);
            final DateTime time = new DateTime(dialog, SWT.TIME);
            new Label(dialog, SWT.NONE);
            new Label(dialog, SWT.NONE);

            Button ok = new Button(dialog, SWT.PUSH);
            ok.setText(BaseMessages.getString(PKG, CONST_OK));
            ok.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            ok.addSelectionListener(
                new SelectionAdapter() {
                  @Override
                  public void widgetSelected(SelectionEvent e) {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, calendar.getYear());
                    cal.set(Calendar.MONTH, calendar.getMonth());
                    cal.set(Calendar.DAY_OF_MONTH, calendar.getDay());

                    cal.set(Calendar.HOUR_OF_DAY, time.getHours());
                    cal.set(Calendar.MINUTE, time.getMinutes());
                    cal.set(Calendar.SECOND, time.getSeconds());

                    wReadFrom.setText(
                        new SimpleDateFormat(ActionGetPOP.DATE_PATTERN).format(cal.getTime()));

                    dialog.close();
                  }
                });
            dialog.setDefaultButton(ok);
            dialog.pack();
            dialog.open();
          }
        });

    wlReadFrom = new Label(wReceivedDate, SWT.RIGHT);
    wlReadFrom.setText(BaseMessages.getString(PKG, "ActionGetPOP.ReadFrom.Label"));
    PropsUi.setLook(wlReadFrom);
    FormData fdlReadFrom = new FormData();
    fdlReadFrom.left = new FormAttachment(0, 0);
    fdlReadFrom.top = new FormAttachment(wConditionOnReceivedDate, margin);
    fdlReadFrom.right = new FormAttachment(middle, -margin);
    wlReadFrom.setLayoutData(fdlReadFrom);
    wReadFrom = new TextVar(variables, wReceivedDate, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wReadFrom.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.ReadFrom.Tooltip"));
    PropsUi.setLook(wReadFrom);
    wReadFrom.addModifyListener(lsMod);
    FormData fdReadFrom = new FormData();
    fdReadFrom.left = new FormAttachment(middle, 0);
    fdReadFrom.top = new FormAttachment(wConditionOnReceivedDate, margin);
    fdReadFrom.right = new FormAttachment(open, -margin);
    wReadFrom.setLayoutData(fdReadFrom);

    opento = new Button(wReceivedDate, SWT.PUSH);
    opento.setImage(GuiResource.getInstance().getImageCalendar());
    opento.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.OpenCalendar"));
    FormData fdlButtonto = new FormData();
    fdlButtonto.top = new FormAttachment(wReadFrom, 2 * margin);
    fdlButtonto.right = new FormAttachment(100, 0);
    opento.setLayoutData(fdlButtonto);
    opento.addSelectionListener(
        new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            final Shell dialogto = new Shell(shell, SWT.DIALOG_TRIM);
            dialogto.setText(BaseMessages.getString(PKG, "ActionGetPOP.SelectDate"));
            dialogto.setImage(GuiResource.getInstance().getImageHopUi());
            dialogto.setLayout(new GridLayout(3, false));

            final DateTime calendarto = new DateTime(dialogto, SWT.CALENDAR | SWT.BORDER);
            final DateTime timeto = new DateTime(dialogto, SWT.TIME);
            new Label(dialogto, SWT.NONE);
            new Label(dialogto, SWT.NONE);
            Button okto = new Button(dialogto, SWT.PUSH);
            okto.setText(BaseMessages.getString(PKG, CONST_OK));
            okto.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            okto.addSelectionListener(
                new SelectionAdapter() {
                  @Override
                  public void widgetSelected(SelectionEvent e) {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.YEAR, calendarto.getYear());
                    cal.set(Calendar.MONTH, calendarto.getMonth());
                    cal.set(Calendar.DAY_OF_MONTH, calendarto.getDay());

                    cal.set(Calendar.HOUR_OF_DAY, timeto.getHours());
                    cal.set(Calendar.MINUTE, timeto.getMinutes());
                    cal.set(Calendar.SECOND, timeto.getSeconds());

                    wReadTo.setText(
                        new SimpleDateFormat(ActionGetPOP.DATE_PATTERN).format(cal.getTime()));
                    dialogto.close();
                  }
                });
            dialogto.setDefaultButton(okto);
            dialogto.pack();
            dialogto.open();
          }
        });

    wlReadTo = new Label(wReceivedDate, SWT.RIGHT);
    wlReadTo.setText(BaseMessages.getString(PKG, "ActionGetPOP.ReadTo.Label"));
    PropsUi.setLook(wlReadTo);
    FormData fdlReadTo = new FormData();
    fdlReadTo.left = new FormAttachment(0, 0);
    fdlReadTo.top = new FormAttachment(wReadFrom, 2 * margin);
    fdlReadTo.right = new FormAttachment(middle, -margin);
    wlReadTo.setLayoutData(fdlReadTo);
    wReadTo = new TextVar(variables, wReceivedDate, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    wReadTo.setToolTipText(BaseMessages.getString(PKG, "ActionGetPOP.ReadTo.Tooltip"));
    PropsUi.setLook(wReadTo);
    wReadTo.addModifyListener(lsMod);
    FormData fdReadTo = new FormData();
    fdReadTo.left = new FormAttachment(middle, 0);
    fdReadTo.top = new FormAttachment(wReadFrom, 2 * margin);
    fdReadTo.right = new FormAttachment(opento, -margin);
    wReadTo.setLayoutData(fdReadTo);

    FormData fdReceivedDate = new FormData();
    fdReceivedDate.left = new FormAttachment(0, margin);
    fdReceivedDate.top = new FormAttachment(wContent, margin);
    fdReceivedDate.right = new FormAttachment(100, -margin);
    wReceivedDate.setLayoutData(fdReceivedDate);
    // ///////////////////////////////////////////////////////////
    // / END OF RECEIVED DATE GROUP
    // ///////////////////////////////////////////////////////////

    FormData fdSearchComp = new FormData();
    fdSearchComp.left = new FormAttachment(0, 0);
    fdSearchComp.top = new FormAttachment(wName, 0);
    fdSearchComp.right = new FormAttachment(100, 0);
    fdSearchComp.bottom = new FormAttachment(100, 0);
    wSearchComp.setLayoutData(fdSearchComp);

    wSearchComp.layout();
    wSearchTab.setControl(wSearchComp);
    PropsUi.setLook(wSearchComp);

    // ////////////////////////////////
    // / END OF SEARCH TAB
    // ////////////////////////////////

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment(0, 0);
    fdTabFolder.top = new FormAttachment(wName, margin);
    fdTabFolder.right = new FormAttachment(100, 0);
    fdTabFolder.bottom = new FormAttachment(wOk, -2 * margin);
    wTabFolder.setLayoutData(fdTabFolder);

    getData();
    setUserProxy();
    chooseListMails();
    activeAttachmentFolder();
    refreshProtocol(false);
    conditionReceivedDate();
    wTabFolder.setSelection(0);

    BaseDialog.defaultShellHandling(shell, c -> ok(), c -> cancel());

    return action;
  }

  private void setUserProxy() {
    wlProxyUsername.setEnabled(wUseProxy.getSelection());
    wProxyUsername.setEnabled(wUseProxy.getSelection());
  }

  private boolean connect() {
    String errordescription = null;
    boolean retval = false;
    if (mailConn != null && mailConn.isConnected()) {
      retval = mailConn.isConnected();
    }

    if (!retval) {
      String realserver = variables.resolve(wServerName.getText());
      String realuser = variables.resolve(wUserName.getText());
      String realpass = action.getRealPassword(variables.resolve(wPassword.getText()));
      int realport = Const.toInt(variables.resolve(wPort.getText()), -1);
      String realproxyuser = variables.resolve(wProxyUsername.getText());
      try {
        mailConn =
            new MailConnection(
                LogChannel.UI,
                MailConnectionMeta.getProtocolFromString(
                    wProtocol.getText(), MailConnectionMeta.PROTOCOL_IMAP),
                realserver,
                realport,
                realuser,
                realpass,
                wUseSSL.getSelection(),
                wUseXOAUTH2.getSelection(),
                wUseProxy.getSelection(),
                realproxyuser);
        mailConn.connect();

        retval = true;
      } catch (Exception e) {
        errordescription = e.getMessage();
      }
    }

    if (!retval) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setMessage(
          BaseMessages.getString(
                  PKG, "ActionGetPOP.Connected.NOK.ConnectionBad", wServerName.getText())
              + Const.CR
              + Const.NVL(errordescription, ""));
      mb.setText(BaseMessages.getString(PKG, "ActionGetPOP.Connected.Title.Bad"));
      mb.open();
    }

    return (mailConn.isConnected());
  }

  private void test() {
    if (connect()) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
      mb.setMessage(
          BaseMessages.getString(PKG, "ActionGetPOP.Connected.OK", wServerName.getText())
              + Const.CR);
      mb.setText(BaseMessages.getString(PKG, "ActionGetPOP.Connected.Title.Ok"));
      mb.open();
    }
  }

  private void selectFolder(TextVar input) {
    if (connect()) {
      try {
        Folder folder = mailConn.getStore().getDefaultFolder();
        SelectFolderDialog s = new SelectFolderDialog(shell, SWT.NONE, folder);
        String folderName = s.open();
        if (folderName != null) {
          input.setText(folderName);
        }
      } catch (Exception e) {
        // Ignore errors
      }
    }
  }

  private void checkFolder(String folderName) {
    if (!Utils.isEmpty(folderName)) {
      if (connect()) {
        // check folder
        if (mailConn.folderExists(folderName)) {
          MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
          mb.setMessage(
              BaseMessages.getString(PKG, "ActionGetPOP.IMAPFolderExists.OK", folderName)
                  + Const.CR);
          mb.setText(BaseMessages.getString(PKG, "ActionGetPOP.IMAPFolderExists.Title.Ok"));
          mb.open();
        } else {
          MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
          mb.setMessage(
              BaseMessages.getString(PKG, "ActionGetPOP.Connected.NOK.IMAPFolderExists", folderName)
                  + Const.CR);
          mb.setText(BaseMessages.getString(PKG, "ActionGetPOP.IMAPFolderExists.Title.Bad"));
          mb.open();
        }
      }
    }
  }

  private void closeMailConnection() {
    try {
      if (mailConn != null) {
        mailConn.disconnect();
        mailConn = null;
      }
    } catch (Exception e) {
      // Ignore
    }
  }

  private void conditionReceivedDate() {
    boolean activeReceivedDate =
        (MailConnectionMeta.getConditionDateByDesc(wConditionOnReceivedDate.getText())
            != MailConnectionMeta.CONDITION_DATE_IGNORE);
    boolean useBetween =
        (MailConnectionMeta.getConditionDateByDesc(wConditionOnReceivedDate.getText())
            == MailConnectionMeta.CONDITION_DATE_BETWEEN);
    wlReadFrom.setVisible(activeReceivedDate);
    wReadFrom.setVisible(activeReceivedDate);
    open.setVisible(activeReceivedDate);
    wlReadTo.setVisible(activeReceivedDate && useBetween);
    wReadTo.setVisible(activeReceivedDate && useBetween);
    opento.setVisible(activeReceivedDate && useBetween);
    if (!activeReceivedDate) {
      wReadFrom.setText("");
      wReadTo.setText("");
      wNegateReceivedDate.setSelection(false);
    }
  }

  private void activeAttachmentFolder() {
    boolean getmessages =
        MailConnectionMeta.getActionTypeByDesc(wActionType.getText())
            == MailConnectionMeta.ACTION_TYPE_GET;
    wlDifferentFolderForAttachment.setEnabled(getmessages && wGetAttachment.getSelection());
    wDifferentFolderForAttachment.setEnabled(getmessages && wGetAttachment.getSelection());
    boolean activeattachmentfolder =
        (wGetAttachment.getSelection() && wDifferentFolderForAttachment.getSelection());
    wlAttachmentFolder.setEnabled(getmessages && activeattachmentfolder);
    wAttachmentFolder.setEnabled(getmessages && activeattachmentfolder);
    wbAttachmentFolder.setEnabled(getmessages && activeattachmentfolder);
    if (!wGetAttachment.getSelection() && !wGetMessage.getSelection()) {
      wGetMessage.setSelection(true);
    }
  }

  private void refreshPort(boolean refreshport) {
    if (refreshport) {
      if (wProtocol.getText().equals(MailConnectionMeta.PROTOCOL_STRING_POP3)) {
        if (wUseSSL.getSelection()) {
          if (Utils.isEmpty(wPort.getText())
              || wPort.getText().equals("" + MailConnectionMeta.DEFAULT_SSL_IMAP_PORT)) {
            wPort.setText("" + MailConnectionMeta.DEFAULT_SSL_POP3_PORT);
          }
        } else {
          if (Utils.isEmpty(wPort.getText())
              || wPort.getText().equals(MailConnectionMeta.DEFAULT_IMAP_PORT)) {
            wPort.setText("" + MailConnectionMeta.DEFAULT_POP3_PORT);
          }
        }
      } else {
        if (wUseSSL.getSelection()) {
          if (Utils.isEmpty(wPort.getText())
              || wPort.getText().equals("" + MailConnectionMeta.DEFAULT_SSL_POP3_PORT)) {
            wPort.setText("" + MailConnectionMeta.DEFAULT_SSL_IMAP_PORT);
          }
        } else {
          if (Utils.isEmpty(wPort.getText())
              || wPort.getText().equals(MailConnectionMeta.DEFAULT_POP3_PORT)) {
            wPort.setText("" + MailConnectionMeta.DEFAULT_IMAP_PORT);
          }
        }
      }
    }
  }

  private void refreshProtocol(boolean refreshport) {
    checkUnavailableMode();
    boolean activePOP3 = wProtocol.getText().equals(MailConnectionMeta.PROTOCOL_STRING_POP3);
    wlPOP3Message.setEnabled(activePOP3);
    wlListmails.setEnabled(activePOP3);
    wListmails.setEnabled(activePOP3);
    wlFirstmails.setEnabled(activePOP3);
    wlDelete.setEnabled(activePOP3);
    wDelete.setEnabled(activePOP3);

    wlIMAPFirstmails.setEnabled(!activePOP3);
    wIMAPFirstmails.setEnabled(!activePOP3);
    wlIMAPFolder.setEnabled(!activePOP3);
    wIMAPFolder.setEnabled(!activePOP3);
    wlIncludeSubFolders.setEnabled(!activePOP3);
    wIncludeSubFolders.setEnabled(!activePOP3);
    wlIMAPListmails.setEnabled(!activePOP3);
    wIMAPListmails.setEnabled(!activePOP3);
    wTestIMAPFolder.setEnabled(!activePOP3);
    wSelectFolder.setEnabled(!activePOP3);
    wlAfterGetIMAP.setEnabled(!activePOP3);
    wAfterGetIMAP.setEnabled(!activePOP3);

    if (activePOP3) {
      // clear out selections
      wConditionOnReceivedDate.select(0);
      conditionReceivedDate();
    }
    // POP3 protocol does not provide information about when a message was received
    wConditionOnReceivedDate.setEnabled(!activePOP3);
    wNegateReceivedDate.setEnabled(!activePOP3);
    wlConditionOnReceivedDate.setEnabled(!activePOP3);

    chooseListMails();
    refreshPort(refreshport);
    setActionType();
  }

  private void checkUnavailableMode() {
    if (wProtocol.getText().equals(MailConnectionMeta.PROTOCOL_STRING_POP3)
        && MailConnectionMeta.getActionTypeByDesc(wActionType.getText())
            == MailConnectionMeta.ACTION_TYPE_MOVE) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setMessage(
          "This action is not available for POP3!"
              + Const.CR
              + "Only one Folder (INBOX) is available in POP3."
              + Const.CR
              + "If you want to move messages to another folder,"
              + Const.CR
              + "please use IMAP protocol.");
      mb.setText("ERROR");
      mb.open();
      wActionType.setText(MailConnectionMeta.getActionTypeDesc(MailConnectionMeta.ACTION_TYPE_GET));
    }
  }

  private void setActionType() {
    checkUnavailableMode();
    if (MailConnectionMeta.getActionTypeByDesc(wActionType.getText())
        != MailConnectionMeta.ACTION_TYPE_GET) {
      wAfterGetIMAP.setText(
          MailConnectionMeta.getAfterGetIMAPDesc(MailConnectionMeta.AFTER_GET_IMAP_NOTHING));
    }

    boolean getmessages =
        MailConnectionMeta.getActionTypeByDesc(wActionType.getText())
            == MailConnectionMeta.ACTION_TYPE_GET;

    wlOutputDirectory.setEnabled(getmessages);
    wOutputDirectory.setEnabled(getmessages);
    wbDirectory.setEnabled(getmessages);
    wlCreateLocalFolder.setEnabled(getmessages);
    wCreateLocalFolder.setEnabled(getmessages);
    wFilenamePattern.setEnabled(getmessages);
    wlFilenamePattern.setEnabled(getmessages);
    wlAttachmentWildcard.setEnabled(getmessages);
    wAttachmentWildcard.setEnabled(getmessages);
    wlDifferentFolderForAttachment.setEnabled(getmessages);
    wDifferentFolderForAttachment.setEnabled(getmessages);
    wlGetAttachment.setEnabled(getmessages);
    wGetAttachment.setEnabled(getmessages);
    wlGetMessage.setEnabled(getmessages);
    wGetMessage.setEnabled(getmessages);

    wlAfterGetIMAP.setEnabled(
        getmessages && wProtocol.getText().equals(MailConnectionMeta.PROTOCOL_STRING_IMAP));
    wAfterGetIMAP.setEnabled(
        getmessages && wProtocol.getText().equals(MailConnectionMeta.PROTOCOL_STRING_IMAP));

    setAfterIMAPRetrived();
  }

  private void setAfterIMAPRetrived() {
    boolean activeMoveToFolfer =
        (((wProtocol.getText().equals(MailConnectionMeta.PROTOCOL_STRING_IMAP))
                && (MailConnectionMeta.getActionTypeByDesc(wActionType.getText())
                    == MailConnectionMeta.ACTION_TYPE_MOVE))
            || (MailConnectionMeta.getAfterGetIMAPByDesc(wAfterGetIMAP.getText())
                == MailConnectionMeta.AFTER_GET_IMAP_MOVE));
    wlMoveToFolder.setEnabled(activeMoveToFolfer);
    wMoveToFolder.setEnabled(activeMoveToFolfer);
    wTestMoveToFolder.setEnabled(activeMoveToFolfer);
    wSelectMoveToFolder.setEnabled(activeMoveToFolfer);
    wlCreateMoveToFolder.setEnabled(activeMoveToFolfer);
    wCreateMoveToFolder.setEnabled(activeMoveToFolfer);
  }

  public void chooseListMails() {
    boolean ok =
        (wProtocol.getText().equals(MailConnectionMeta.PROTOCOL_STRING_POP3)
            && wListmails.getSelectionIndex() == 1);
    wlFirstmails.setEnabled(ok);
    wFirstmails.setEnabled(ok);
  }

  @Override
  public void dispose() {
    closeMailConnection();
    super.dispose();
  }

  /** Copy information from the meta-data input to the dialog fields. */
  public void getData() {
    if (action.getName() != null) {
      wName.setText(action.getName());
    }
    wSelectionLine.setText(Const.nullToEmpty(action.getConnectionName()));
    if (action.getServerName() != null) {
      wServerName.setText(action.getServerName());
    }
    if (action.getUserName() != null) {
      wUserName.setText(action.getUserName());
    }
    if (action.getPassword() != null) {
      wPassword.setText(action.getPassword());
    }

    wUseSSL.setSelection(action.isUseSsl());
    wUseXOAUTH2.setSelection(action.isUseXOauth2());
    wGetMessage.setSelection(action.isSaveMessage());
    wGetAttachment.setSelection(action.isSaveAttachment());
    wDifferentFolderForAttachment.setSelection(action.isUseDifferentFolderForAttachment());
    if (action.getAttachmentFolder() != null) {
      wAttachmentFolder.setText(action.getAttachmentFolder());
    }

    if (action.getSslPort() != null) {
      wPort.setText(action.getSslPort());
    }

    if (action.getOutputDirectory() != null) {
      wOutputDirectory.setText(action.getOutputDirectory());
    }
    if (action.getFilenamePattern() != null) {
      wFilenamePattern.setText(action.getFilenamePattern());
    }
    if (action.getAttachmentWildcard() != null) {
      wAttachmentWildcard.setText(action.getAttachmentWildcard());
    }

    String protocol = action.getProtocol();
    boolean isPop3 = StringUtils.equals(protocol, MailConnectionMeta.PROTOCOL_STRING_POP3);
    wProtocol.setText(protocol);
    int i = action.getRetrieveMails();

    if (i > 0) {
      if (isPop3) {
        wListmails.select(i - 1);
      } else {
        wListmails.select(i);
      }
    } else {
      wListmails.select(0); // Retrieve All Mails
    }

    if (action.getFirstMails() != null) {
      wFirstmails.setText(action.getFirstMails());
    }

    wDelete.setSelection(action.isDelete());
    wIMAPListmails.setText(MailConnectionMeta.getValueImapListDesc(action.getValueIMAPList()));
    if (action.getImapFolder() != null) {
      wIMAPFolder.setText(action.getImapFolder());
    }
    // search term
    if (action.getSenderSearch() != null) {
      wSender.setText(action.getSenderSearch());
    }
    wNegateSender.setSelection(action.isNotTermSenderSearch());
    if (action.getRecipientSearch() != null) {
      wRecipient.setText(action.getRecipientSearch());
    }
    wNegateReceipient.setSelection(action.isNotTermRecipientSearch());
    if (action.getSubjectSearch() != null) {
      wSubject.setText(action.getSubjectSearch());
    }
    wNegateSubject.setSelection(action.isNotTermSubjectSearch());
    if (action.getBodySearch() != null) {
      wBody.setText(action.getBodySearch());
    }
    wNegateBody.setSelection(action.isNotTermBodySearch());
    wConditionOnReceivedDate.setText(
        MailConnectionMeta.getConditionDateDesc(action.getConditionReceivedDate()));
    wNegateReceivedDate.setSelection(action.isNotTermReceivedDateSearch());
    if (action.getReceivedDate1() != null) {
      wReadFrom.setText(action.getReceivedDate1());
    }
    if (action.getReceivedDate2() != null) {
      wReadTo.setText(action.getReceivedDate2());
    }
    wActionType.setText(MailConnectionMeta.getActionTypeDesc(action.getActionType()));
    wCreateMoveToFolder.setSelection(action.isCreateMoveToFolder());
    wCreateLocalFolder.setSelection(action.isCreateLocalFolder());
    if (action.getMoveToIMAPFolder() != null) {
      wMoveToFolder.setText(action.getMoveToIMAPFolder());
    }
    wAfterGetIMAP.setText(MailConnectionMeta.getAfterGetIMAPDesc(action.getAfterGetIMAP()));
    wIncludeSubFolders.setSelection(action.isIncludeSubFolders());
    wUseProxy.setSelection(action.isUseProxy());
    if (action.getProxyUsername() != null) {
      wProxyUsername.setText(action.getProxyUsername());
    }
    if (action.getImapFirstMails() != null) {
      wIMAPFirstmails.setText(action.getImapFirstMails());
    }

    wName.selectAll();
    wName.setFocus();
  }

  private void cancel() {
    action.setChanged(changed);
    action = null;
    dispose();
  }

  private void ok() {
    if (Utils.isEmpty(wName.getText())) {
      MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
      mb.setMessage(BaseMessages.getString(PKG, "ActionGetPOP.NoNameMessageBox.Message"));
      mb.setText(BaseMessages.getString(PKG, "ActionGetPOP.NoNameMessageBox.Text"));
      mb.open();
      return;
    }
    action.setName(wName.getText());
    action.setConnectionName(wSelectionLine.getText());
    action.setServerName(wServerName.getText());
    action.setUserName(wUserName.getText());
    action.setPassword(wPassword.getText());
    action.setUseSsl(wUseSSL.getSelection());
    action.setUseXOauth2(wUseXOAUTH2.getSelection());
    action.setSaveAttachment(wGetAttachment.getSelection());
    action.setSaveMessage(wGetMessage.getSelection());
    action.setUseDifferentFolderForAttachment(wDifferentFolderForAttachment.getSelection());
    action.setAttachmentFolder(wAttachmentFolder.getText());
    action.setSslPort(wPort.getText());
    action.setOutputDirectory(wOutputDirectory.getText());
    action.setFilenamePattern(wFilenamePattern.getText());

    // Option 'retrieve unread' is removed and there is only 2 options.
    // for backward compatibility: 0 is 'retrieve all', 1 is 'retrieve first...'
    int actualIndex = wListmails.getSelectionIndex();
    action.setRetrieveMails(actualIndex > 0 ? 2 : 0);

    action.setFirstMails(wFirstmails.getText());
    action.setDelete(wDelete.getSelection());
    action.setProtocol(wProtocol.getText());
    action.setAttachmentWildcard(wAttachmentWildcard.getText());
    action.setValueIMAPList(MailConnectionMeta.getValueImapListByDesc(wIMAPListmails.getText()));
    action.setImapFirstMails(wIMAPFirstmails.getText());
    action.setImapFolder(wIMAPFolder.getText());
    // search term
    action.setSenderSearch(wSender.getText());
    action.setNotTermSenderSearch(wNegateSender.getSelection());

    action.setRecipientSearch(wRecipient.getText());
    action.setNotTermRecipientSearch(wNegateReceipient.getSelection());
    action.setSubjectSearch(wSubject.getText());
    action.setNotTermSubjectSearch(wNegateSubject.getSelection());
    action.setBodySearch(wBody.getText());
    action.setNotTermBodySearch(wNegateBody.getSelection());
    action.setConditionReceivedDate(
        MailConnectionMeta.getConditionDateByDesc(wConditionOnReceivedDate.getText()));
    action.setNotTermReceivedDateSearch(wNegateReceivedDate.getSelection());
    action.setReceivedDate1(wReadFrom.getText());
    action.setReceivedDate2(wReadTo.getText());
    action.setActionType(MailConnectionMeta.getActionTypeByDesc(wActionType.getText()));
    action.setMoveToIMAPFolder(wMoveToFolder.getText());
    action.setCreateMoveToFolder(wCreateMoveToFolder.getSelection());
    action.setCreateLocalFolder(wCreateLocalFolder.getSelection());
    action.setAfterGetIMAP(MailConnectionMeta.getAfterGetIMAPByDesc(wAfterGetIMAP.getText()));
    action.setIncludeSubFolders(wIncludeSubFolders.getSelection());
    action.setUseProxy(wUseProxy.getSelection());
    action.setProxyUsername(wProxyUsername.getText());
    dispose();
  }
}
