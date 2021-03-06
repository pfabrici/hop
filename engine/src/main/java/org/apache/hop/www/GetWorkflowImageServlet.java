/*! ******************************************************************************
 *
 * Hop : The Hop Orchestration Platform
 *
 * http://www.project-hop.org
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.www;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.hop.core.gui.AreaOwner;
import org.apache.hop.core.gui.Point;
import org.apache.hop.core.gui.SwingGc;
import org.apache.hop.core.util.Utils;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.workflow.WorkflowMeta;
import org.apache.hop.workflow.WorkflowPainter;
import org.apache.hop.workflow.engine.IWorkflowEngine;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;

public class GetWorkflowImageServlet extends BaseHttpServlet implements IHopServerPlugin {

  private static final long serialVersionUID = -4365372274638005929L;
  public static final float ZOOM_FACTOR = 1.5f;

  private static Class<?> PKG = GetPipelineStatusServlet.class; // for i18n purposes, needed by Translator!!

  public static final String CONTEXT_PATH = "/hop/workflowImage";

  public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
    IOException {
    if ( isJettyMode() && !request.getContextPath().startsWith( CONTEXT_PATH ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "GetWorkflowImageServlet.Log.WorkflowImageRequested" ) );
    }

    String workflowName = request.getParameter( "name" );
    String id = request.getParameter( "id" );

    // ID is optional...
    //
    IWorkflowEngine<WorkflowMeta> workflow;
    HopServerObjectEntry entry;
    if ( Utils.isEmpty( id ) ) {
      // get the first pipeline that matches...
      //
      entry = getWorkflowMap().getFirstHopServerObjectEntry( workflowName );
      if ( entry == null ) {
        workflow = null;
      } else {
        id = entry.getId();
        workflow = getWorkflowMap().getWorkflow( entry );
      }
    } else {
      // Take the ID into account!
      //
      entry = new HopServerObjectEntry( workflowName, id );
      workflow = getWorkflowMap().getWorkflow( entry );
    }

    ByteArrayOutputStream svgStream = null;

    try {
      if ( workflow != null ) {

        response.setStatus( HttpServletResponse.SC_OK );

        response.setCharacterEncoding( "UTF-8" );
        response.setContentType( "image/svg+xml" );

        // Generate workflow SVG image
        //
        String svgXml = generateWorkflowSvgImage( workflow.getWorkflowMeta() );
        svgStream = new ByteArrayOutputStream();
        try {
          svgStream.write( svgXml.getBytes("UTF-8") );
        } finally {
          svgStream.flush();
        }
        response.setContentLength( svgStream.size() );

        OutputStream out = response.getOutputStream();
        out.write( svgStream.toByteArray() );
      }
    } catch ( Exception e ) {
      e.printStackTrace();
    } finally {
      if (svgStream!=null) {
        svgStream.close();
      }
    }
  }

  private String generateWorkflowSvgImage( WorkflowMeta workflowMeta ) throws Exception {
    float magnification = ZOOM_FACTOR;
    Point maximum = workflowMeta.getMaximum();
    maximum.multiply( magnification );

    DOMImplementation domImplementation = GenericDOMImplementation.getDOMImplementation();

    // Create an instance of org.w3c.dom.Document.
    String svgNamespace = "http://www.w3.org/2000/svg";
    Document document = domImplementation.createDocument(svgNamespace, "svg", null);

    SVGGraphics2D graphics2D = new SVGGraphics2D( document );

    SwingGc gc = new SwingGc( graphics2D, new Rectangle(0,0,maximum.x,maximum.y), 32, 0, 0 );
    WorkflowPainter workflowPainter = new WorkflowPainter( gc, workflowMeta, maximum, null, null, null, null, null, new ArrayList<AreaOwner>(), 32, 1, 0, "Arial", 10, 1.0d );
    workflowPainter.setMagnification( magnification );
    workflowPainter.drawWorkflow();

    // convert to SVG
    //
    StringWriter stringWriter = new StringWriter();
    graphics2D.stream( stringWriter, true );

    return stringWriter.toString();
  }

  public String toString() {
    return "Workflow Image IHandler";
  }

  public String getService() {
    return CONTEXT_PATH + " (" + toString() + ")";
  }

  public String getContextPath() {
    return CONTEXT_PATH;
  }
}
