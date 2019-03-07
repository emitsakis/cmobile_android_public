package certh.hit.cmobile.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by anmpout on 07/03/2019
 */
@Root(name = "osm", strict = false)
public class Osm {
    @Attribute(name = "version",required = false)
    private String version;
    @Element(name = "way")
    private Way way;
    public Osm() {

    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
 class Way{
     @Attribute(name = "id",required = false)
     private String id;
     @ElementList(inline=true)
     private List<Nd> list;
     public Way() {

     }

     public String getId() {
         return id;
     }

     public void setId(String id) {
         this.id = id;
     }
 }

 class Nd{
@Attribute(name = "ref",required = false)
private String ref;

     public Nd() {
     }

     public String getRef() {
         return ref;
     }

     public void setRef(String ref) {
         this.ref = ref;
     }
 }
