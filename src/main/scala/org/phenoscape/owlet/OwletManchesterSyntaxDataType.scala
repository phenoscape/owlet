package org.phenoscape.owlet


import java.io.StringWriter

import org.apache.jena.datatypes.TypeMapper
import org.apache.jena.graph.{Node, NodeFactory}
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxObjectRenderer
import org.semanticweb.owlapi.model.{OWLClassExpression, OWLClassExpressionVisitor, OWLEntity}
import org.semanticweb.owlapi.util.ShortFormProvider

object OwletManchesterSyntaxDataType {

  object FullIRIProvider extends ShortFormProvider {

    def getShortForm(entity: OWLEntity): String = s"<${entity.getIRI.toString}>"

    def dispose(): Unit = ()

  }

  implicit class SerializableClassExpression(val self: OWLClassExpression) extends AnyVal {

    def asOMN: Node = {
      val writer = new StringWriter()
      val renderer = new ManchesterOWLSyntaxObjectRenderer(writer, FullIRIProvider)
      self.accept(renderer: OWLClassExpressionVisitor)
      writer.close()
      NodeFactory.createLiteral(writer.toString.replaceAll("\n", " ").replaceAll("\\s+", " "),
        TypeMapper.getInstance.getSafeTypeByName(Owlet.MANCHESTER))
    }

  }

}