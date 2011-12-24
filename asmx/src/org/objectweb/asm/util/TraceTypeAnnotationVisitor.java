package org.objectweb.asm.util;

import org.objectweb.asm.TypeAnnotationVisitor;
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
      
      switch(xtarget_type) {
      // 0x00/0x01: typecast
      // 0x02/0x03: type test (instanceof)
      // 0x04/0x05: object creation
      // {
      //   u2 offset;
      // } reference_info;
      case 0x00:
      case 0x01:
      case 0x02:
      case 0x03:
      case 0x04:
      case 0x05:
          buf.append(doubleTab).append("offset: ").append(xoffset).append("\n");
          break;
        
      // 0x06/0x07*: method receiver
      // {
      // } reference_info;
      case 0x06:
      case 0x07:
          break;
      
      // 0x08/0x09: local variable
      // {
      //   u2 start_pc;
      //   u2 length;
      //   u2 index;
      // } reference_info;
      case 0x08:
      case 0x09:
          buf.append(doubleTab).append("start_pc: ").append(xstart_pc).append("\n");
          buf.append(doubleTab).append("length: ").append(xlength).append("\n");
          buf.append(doubleTab).append("index: ").append(xindex).append("\n");
          break;
      
      // 0x0A*/0x0B: method return type
      // {
      // } reference_info;
      case 0x0A:
      case 0x0B:
          break;
      
      // 0x0C*/0x0D: method parameter
      // {
      //   TEMP this should contain the index but doesn't, so for the moment
      //        we don't print an index
      // } reference_info;
      case 0x0C:
      case 0x0D:
          buf.append(doubleTab).append("index: ").append("FIXME").append("\n");
          break;
      
      // 0x0E*/0x0F: field
      // {
      // } reference_info;
      case 0x0E:
      case 0x0F:
          break;
      
      // 0x10/0x11: class type parameter bound
      // 0x12/0x13: method type parameter bound
      // {
      //   u1 bound_index;
      // } reference_info;
      case 0x10:
      case 0x11:
      case 0x12:
      case 0x13:
          buf.append(doubleTab).append("param_index: ").append(xparam_index).append("\n");
          buf.append(doubleTab).append("bound_index: ").append(xbound_index).append("\n");
          break;
      
      // 0x14/0x15: class extends/implements
      // 0x16/0x17*: exception type in throws/implements
      // {
      //   u1 type_index;  
      // }
      case 0x14:
      case 0x15:
      case 0x16:
      case 0x17:
        buf.append(doubleTab).append("type_index: ").append(xtype_index).append("\n");
        break;
        
      // 0x18/0x19: type argument in constructor call
      // 0x1A/0x1B: type argument in method call
      // {
      // } reference_info;
      case 0x18:
      case 0x19:
      case 0x1A:
      case 0x1B:
        break;
          
      // 0x1C/0x1D: wildcard bound
      // {
      //    u1 bound_index;
      // } reference_info;
      case 0x1C:
      case 0x1D:
        buf.append(doubleTab).append("bound_index: ").append(xbound_index).append("\n");
        break;
          
      // 0x1E/0x1F*: class literal
      // {
      // } reference_info;
      case 0x1E:
      case 0x1F:
        break;
          
      // 0x20/0x21*: method type parameter
      // {        
      //    u1 param_index;
      // } reference_info;
      case 0x20:
      case 0x21:
        buf.append(doubleTab).append("param_index: ").append(xparam_index).append("\n");
        break;
        
        
        default: throw new RuntimeException("Unrecognized target type: + " + xtarget_type);
      }
      
      // now print out location string for generic target types
      switch(xtarget_type) {
      case 0x01:
      case 0x03:
      case 0x05:
      case 0x07:
      case 0x09:
      case 0x0B:
      case 0x0D:
      case 0x0F:
      case 0x11:
      case 0x13:
      case 0x15:
      case 0x17:
      case 0x19:
      case 0x1B:
      case 0x1D:
      case 0x1F:
      case 0x21:
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
