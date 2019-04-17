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
    @Attribute(name = "generator",required = false)
    private String generator;
    @Attribute(name = "copyright",required = false)
    private String copyright;
    @Attribute(name = "attribution",required = false)
    private String attribution;
    @Attribute(name = "license",required = false)
    private String license;
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

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public Way getWay() {
        return way;
    }

    public void setWay(Way way) {
        this.way = way;
    }
}
 class Way{
     @Attribute(name = "id",required = false)
     private String id;
     @Attribute(name = "visible",required = false)
     private String visible;
     @Attribute(name = "version",required = false)
     private String version;
     @Attribute(name = "changeset",required = false)
     private String changeset;
     @Attribute(name = "timestamp",required = false)
     private String timestamp;
     @Attribute(name = "user",required = false)
     private String user;
     @Attribute(name = "uid",required = false)
     private String uid;

     @ElementList(inline=true)
     private List<Nd> list;
     @ElementList(inline=true)
     private List<Tag> tags;
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
class Tag{
    @Attribute(name = "k",required = false)
    private String k;

    @Attribute(name = "v",required = false)
    private String v;

    public Tag() {
    }

    public String getK() {
        return k;
    }

    public void setK(String k) {
        this.k = k;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }
}