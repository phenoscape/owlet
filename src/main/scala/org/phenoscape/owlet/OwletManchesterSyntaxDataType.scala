package org.phenoscape.owlet

import com.hp.hpl.jena.datatypes.BaseDatatype
import com.hp.hpl.jena.datatypes.TypeMapper
import org.semanticweb.owlapi.model.OWLClassExpression
import com.hp.hpl.jena.graph.Node
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxObjectRenderer
import org.semanticweb.owlapi.util.QNameShortFormProvider
import scala.collection.JavaConversions._
import java.io.StringWriter
import com.hp.hpl.jena.graph.NodeFactory
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor
import org.semanticweb.owlapi.util.ShortFormProvider
import org.semanticweb.owlapi.model.OWLEntity

object OwletManchesterSyntaxDataType extends BaseDatatype(Owlet.MANCHESTER) {

  TypeMapper.getInstance.registerDatatype(this);

  object FullIRIProvider extends ShortFormProvider {

    def getShortForm(entity: OWLEntity): String = s"<${entity.getIRI.toString}>"

    def dispose(): Unit = Unit

  }

  implicit class SerializableClassExpression(val self: OWLClassExpression) extends AnyVal {

    def asOMN: Node = {
      val writer = new StringWriter()
      val renderer = new ManchesterOWLSyntaxObjectRenderer(writer, FullIRIProvider)
      renderer.setUseWrapping(false)
      self.accept(renderer: OWLClassExpressionVisitor)
      NodeFactory.createLiteral(writer.toString, OwletManchesterSyntaxDataType)
    }

  }

}