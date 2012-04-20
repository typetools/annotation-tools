package org.objectweb.asm.util;

import org.objectweb.asm.TypeAnnotationVisitor;

import com.sun.tools.javac.code.TargetType;

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

    private int valueNumber = 0;

    // variables necessary in order to print reference info for
    // extended annotation
    private int xtarget_type;
    private int xoffset;
    private int xlocation_length;
    private int xlocations[];
    private int xlocations_index;
    private int xnum_entries;
    private int xstart_pc;
    private int xlength;
    private int xindex;
    private int xparam_index;
    private int xbound_index;
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
      // typecast
      // type test (instanceof)
      // object creation
      // {
      //   u2 offset;
      // } reference_info;
      case TYPECAST:
      case TYPECAST_COMPONENT:
      case INSTANCEOF:
      case INSTANCEOF_COMPONENT:
      case NEW:
      case NEW_COMPONENT:
          buf.append(doubleTab).append("offset: ").append(xoffset).append("\n");
          break;

      // method receiver
      // {
      // } reference_info;
      case METHOD_RECEIVER:
      case METHOD_RECEIVER_COMPONENT:
          break;

      // local variable
      // {
      //   u2 start_pc;
      //   u2 length;
      //   u2 index;
      // } reference_info;
      case LOCAL_VARIABLE:
      case LOCAL_VARIABLE_COMPONENT:
          buf.append(doubleTab).append("start_pc: ").append(xstart_pc).append("\n");
          buf.append(doubleTab).append("length: ").append(xlength).append("\n");
          buf.append(doubleTab).append("index: ").append(xindex).append("\n");
          break;

          // method return type
      // {
      // } reference_info;
      case METHOD_RETURN:
      case METHOD_RETURN_COMPONENT:
          break;

      // method parameter
      // {
      //   TEMP this should contain the index but doesn't, so for the moment
      //        we don't print an index
      // } reference_info;
      case METHOD_PARAMETER:
      case METHOD_PARAMETER_COMPONENT:
          buf.append(doubleTab).append("index: ").append("FIXME").append("\n");
          break;

      // field
      // {
      // } reference_info;
      case FIELD:
      case FIELD_COMPONENT:
          break;

      // class type parameter bound
      // method type parameter bound
      // {
      //   u1 bound_index;
      // } reference_info;
      case CLASS_TYPE_PARAMETER_BOUND:
      case CLASS_TYPE_PARAMETER_BOUND_COMPONENT:
      case METHOD_TYPE_PARAMETER_BOUND:
      case METHOD_TYPE_PARAMETER_BOUND_COMPONENT:
          buf.append(doubleTab).append("param_index: ").append(xparam_index).append("\n");
          buf.append(doubleTab).append("bound_index: ").append(xbound_index).append("\n");
          break;

      // class extends/implements
      // exception type in throws/implements
      // {
      //   u1 type_index;  
      // }
      case CLASS_EXTENDS:
      case CLASS_EXTENDS_COMPONENT:
      case THROWS:
      // Undefined case THROWS_COMPONENT:
          buf.append(doubleTab).append("type_index: ").append(xtype_index).append("\n");
          break;

      // type argument in constructor call
      // type argument in method call
      // {
      // } reference_info;
      case NEW_TYPE_ARGUMENT:
      case NEW_TYPE_ARGUMENT_COMPONENT:
      case METHOD_TYPE_ARGUMENT:
      case METHOD_TYPE_ARGUMENT_COMPONENT:
          break;

      // method type parameter
      // {        
      //    u1 param_index;
      // } reference_info;
      case METHOD_TYPE_PARAMETER:
      // Undefined case METHOD_TYPE_PARAMETER_COMPONENT:
          buf.append(doubleTab).append("param_index: ").append(xparam_index).append("\n");
          break;

      default: throw new RuntimeException("Unrecognized target type: + " + xtarget_type);
      }

      // now print out location string for generic target types
      switch(tt) {
      case TYPECAST_COMPONENT:
      case INSTANCEOF_COMPONENT:
      case NEW_COMPONENT:
      case METHOD_RECEIVER_COMPONENT:
      case LOCAL_VARIABLE_COMPONENT:
      case METHOD_RETURN_COMPONENT:
      case METHOD_PARAMETER_COMPONENT:
      case FIELD_COMPONENT:
      case CLASS_TYPE_PARAMETER_BOUND_COMPONENT:
      case METHOD_TYPE_PARAMETER_BOUND_COMPONENT:
      case CLASS_EXTENDS_COMPONENT:
      // Undefined case THROWS_COMPONENT:
      case NEW_TYPE_ARGUMENT_COMPONENT:
      case METHOD_TYPE_ARGUMENT_COMPONENT:
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
      default : // do nothing;
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
      this.xlocations = new int[this.xlocation_length];
      this.xlocations_index = 0;
      if(xav != null) {
        xav.visitXLocationLength(location_length);
      }
    }

    public void visitXLocation(int location) {
      this.xlocations[xlocations_index] = location;
      this.xlocations_index++;
      if(xav != null) {
        xav.visitXLocation(location);
      }
    }

    public void visitXNumEntries(int num_entries) {
      this.xnum_entries = num_entries;
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
}
