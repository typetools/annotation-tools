package org.objectweb.asm.util;

import org.objectweb.asm.TypeAnnotationVisitor;

import com.sun.tools.javac.code.TargetType;
import com.sun.tools.javac.code.TypeAnnotationPosition.TypePathEntry;

/**
 * An {@link TypeAnnotationVisitor} that prints a disassembled view of the
 * extended annotations it visits.
 * 
 * @author jaimeq
 */

public class TraceTypeAnnotationVisitor extends TraceAnnotationVisitor 
  implements TypeAnnotationVisitor
{

    /**
     * The {@link TypeAnnotationVisitor} to which this visitor 
     * delegates calls. May be <tt>null</tt>.
     */
    protected TypeAnnotationVisitor xav;

    /**
     * A string representing two consecutive tabs, as defined in
     * {@link TraceAbstractVisitor}.
     */
    protected String doubleTab = tab + tab;

    // variables necessary in order to print reference info for
    // extended annotation
    private int xtarget_type;
    private int xoffset;
    private int xlocation_length;
    private TypePathEntry xlocations[];
    private int xlocations_index;
    private int xstart_pc;
    private int xlength;
    private int xindex;
    private int xparam_index;
    private int xbound_index;
    private int xexception_index;
    private int xtype_index;

    /**
     * Constructs a new {@link TraceTypeAnnotationVisitor}.
     */
    public TraceTypeAnnotationVisitor() {
        // ignore
    }

    public void visitEnd() {
        super.visitEnd();
        finishExtendedPart();
    }

    // ------------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------------

    protected TraceTypeAnnotationVisitor createTraceTypeAnnotationVisitor() {
        return new TraceTypeAnnotationVisitor();
    }

    /**
     * Prints the reference info of this extended annotation to this.text
     */
    private void finishExtendedPart() {
      buf.setLength(0);
      buf.append("\n  extended annotation: \n");
      buf.append(doubleTab).append("target_type: ")
        .append(xtarget_type).append("\n");

      TargetType tt = TargetType.fromTargetTypeValue(xtarget_type);
      switch(tt) {
      // type test (instanceof)
      // object creation
      // constructor/method reference receiver
      // {
      //   u2 offset;
      // } reference_info;
      case INSTANCEOF:
      case NEW:
      case CONSTRUCTOR_REFERENCE:
      case METHOD_REFERENCE:
          buf.append(doubleTab).append("offset: ").append(xoffset).append("\n");
          break;

      // method receiver
      // {
      // } reference_info;
      case METHOD_RECEIVER:
          break;

      // local variable
      // {
      //   u2 start_pc;
      //   u2 length;
      //   u2 index;
      // } reference_info;
      case LOCAL_VARIABLE:
      // resource variable
      case RESOURCE_VARIABLE:
          buf.append(doubleTab).append("start_pc: ").append(xstart_pc).append("\n");
          buf.append(doubleTab).append("length: ").append(xlength).append("\n");
          buf.append(doubleTab).append("index: ").append(xindex).append("\n");
          break;

      // method return type
      // {
      // } reference_info;
      case METHOD_RETURN:
          break;

      // method parameter
      // {
      //   TEMP this should contain the index but doesn't, so for the moment
      //        we don't print an index
      // } reference_info;
      case METHOD_FORMAL_PARAMETER:
          buf.append(doubleTab).append("index: ").append("FIXME").append("\n");
          break;

      // field
      // {
      // } reference_info;
      case FIELD:
          break;

      // class type parameter bound
      // method type parameter bound
      // {
      //   u1 bound_index;
      // } reference_info;
      case CLASS_TYPE_PARAMETER_BOUND:
      case METHOD_TYPE_PARAMETER_BOUND:
          buf.append(doubleTab).append("param_index: ").append(xparam_index).append("\n");
          buf.append(doubleTab).append("bound_index: ").append(xbound_index).append("\n");
          break;

      // class extends/implements
      // exception type in throws/implements
      // {
      //   u1 type_index;  
      // }
      case CLASS_EXTENDS:
      case THROWS:
          buf.append(doubleTab).append("type_index: ").append(xtype_index).append("\n");
          break;

      // typecast
      // type argument in constructor call
      // type argument in method call
      // type argument in constructor reference
      // type argument in method reference
      // {
      //   u2 offset;
      //   u1 type_index;
      // } reference_info;
      case CAST:
      case CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT:
      case METHOD_INVOCATION_TYPE_ARGUMENT:
      case CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT:
      case METHOD_REFERENCE_TYPE_ARGUMENT:
          buf.append(doubleTab).append("offset: ").append(xoffset).append("\n");
          buf.append(doubleTab).append("type_index: ").append(xtype_index).append("\n");
          break;

      // method type parameter
      // {        
      //    u1 param_index;
      // } reference_info;
      case METHOD_TYPE_PARAMETER:
          buf.append(doubleTab).append("param_index: ").append(xparam_index).append("\n");
          break;

      default: throw new RuntimeException("Unrecognized target type: + " + xtarget_type);
      }

      // now print out location string for generic target types
      if (xlocation_length != 0) {
          buf.append(doubleTab).append("location_length: " + xlocation_length).append("\n");
          buf.append(doubleTab).append("locations: ");
          boolean first = true;
          for(int i = 0; i < xlocations.length ; i++) {
              if(!first) {
                  buf.append(", ");
              }
              first = false;
              buf.append(xlocations[i]);
          }
          buf.append("\n");
      }

      text.add(buf.toString());
    }

    public void visitXTargetType(int target_type) {
      this.xtarget_type = target_type;
      if(xav != null) {
        xav.visitXTargetType(target_type);
      }
    }

    public void visitXOffset(int offset) {
      this.xoffset = offset;
      if(xav != null) {
        xav.visitXOffset(offset);
      }
    }

    public void visitXLocationLength(int location_length) {
      this.xlocation_length = location_length;
      this.xlocations = new TypePathEntry[this.xlocation_length];
      this.xlocations_index = 0;
      if(xav != null) {
        xav.visitXLocationLength(location_length);
      }
    }

    public void visitXLocation(TypePathEntry location) {
      this.xlocations[xlocations_index] = location;
      this.xlocations_index++;
      if(xav != null) {
        xav.visitXLocation(location);
      }
    }

    public void visitXNumEntries(int num_entries) {
      if(xav != null) {
        xav.visitXNumEntries(num_entries);
      }
    }

    public void visitXStartPc(int start_pc) {
      this.xstart_pc = start_pc;
      if(xav != null) {
        xav.visitXStartPc(start_pc);
      }
    }

    public void visitXLength(int length) {
      this.xlength = length;
      if(xav != null) {
        xav.visitXLength(length);
      }
    }

    public void visitXIndex(int index) {
      this.xindex = index;
      if(xav != null) {
        xav.visitXIndex(index);
      }
    }

    public void visitXParamIndex(int param_index) {
      this.xparam_index = param_index;
      if(xav != null) {
        xav.visitXParamIndex(param_index);
      }
    }

    public void visitXBoundIndex(int bound_index) {
      this.xbound_index = bound_index;
      if(xav != null) {
        xav.visitXBoundIndex(bound_index);
      }
    }

    public void visitXTypeIndex(int type_index) {
      this.xtype_index = type_index;
      if(xav != null) {
        xav.visitXTypeIndex(type_index);
      }
    }

    public void visitXExceptionIndex(int exception_index) {
      this.xexception_index = exception_index;
      if(xav != null) {
        xav.visitXExceptionIndex(exception_index);
      }
    }

    public void visitXNameAndArgsSize() {
    }
}
