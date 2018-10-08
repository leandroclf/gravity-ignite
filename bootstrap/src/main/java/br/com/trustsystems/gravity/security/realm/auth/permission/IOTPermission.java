package br.com.trustsystems.gravity.security.realm.auth.permission;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.InvalidPermissionStringException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class IOTPermission implements Permission, Serializable {

    protected final Logger log = LoggerFactory.getLogger(IOTPermission.class);

    protected static final String PUBLISH_SUBSCRIBE_ROLE = "PUBSUB";
    protected static final String PUBLISH_ROLE = "PUBLISH";
    protected static final String SUBSCRIBE_ROLE = "SUBSCRIBE";
    protected static final String MULTI_LEVEL_WILDCARD_TOKEN = "#";
    protected static final String SINGLE_LEVEL_WILDCARD_TOKEN = "+";
    protected static final String PART_DIVIDER_TOKEN = "/";
    protected static final String USERNAME_TOKEN = "%u";
    protected static final String PARTITION_TOKEN = "%p";
    protected static final String CLIENT_ID_TOKEN = "%c";
    protected static final boolean DEFAULT_CASE_SENSITIVE = false;

    private List<String> parts;

    private String type;

    private String username;

    private String partition;

    private String clientId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public IOTPermission(String wildcardString) {
        this(wildcardString, DEFAULT_CASE_SENSITIVE);


    }

    public IOTPermission(String wildcardString, boolean caseSensitive) {
        setParts(wildcardString, caseSensitive);
    }

  public IOTPermission(String partition, String username, String clientId, String wildcardString) {
        this(wildcardString);
      setUsername(username);
      setPartition(partition);
      setClientId(clientId);
    }


    protected void setParts(String wildcardString, boolean caseSensitive) {
        if (wildcardString == null || wildcardString.trim().isEmpty()) {
            throw new InvalidPermissionStringException("string cannot be null or empty.", wildcardString);
        }


        wildcardString = wildcardString.trim();

        int indexOfColon = wildcardString.indexOf(":");

        if (indexOfColon < 0) {
            throw new IllegalArgumentException("Client permission must be prefixed with the permission type.");
        }

        setType(wildcardString.substring(0, indexOfColon));

        wildcardString = wildcardString.substring(indexOfColon + 1);

        if ( wildcardString.trim().isEmpty()) {
            throw new InvalidPermissionStringException("string cannot be null or empty.", wildcardString);
        }

        if (!caseSensitive) {
            wildcardString = wildcardString.toLowerCase();
        }


        this.parts = Arrays.asList(wildcardString.split(Pattern.quote(PART_DIVIDER_TOKEN)));

        boolean isFirst = true;

        for (String part : getParts()) {

            if (part.isEmpty() ) {

                if(isFirst){
                    isFirst = false;
                }else

                throw new InvalidPermissionStringException("permission string cannot" +
                        " contain parts with only dividers. Make sure permission strings" +
                        " are properly formatted ", wildcardString);
            }

         }

        if (this.getParts().isEmpty()) {
            throw new IllegalArgumentException("Client permission string cannot contain only dividers. Make sure permission strings are properly formatted.");
        }
    }


    protected List<String> getParts() {
        return this.parts;
    }


    /**
     * Returns {@code true} if this current instance <em>implies</em> all the functionality and/or resource access
     * described by the specified {@code Permission} argument, {@code false} otherwise.
     * <p>
     * <p>That is, this current instance must be exactly equal to or a <em>superset</em> of the functionalty
     * and/or resource access described by the given {@code Permission} argument.  Yet another way of saying this
     * would be:
     * <p>
     * <p>If &quot;permission1 implies permission2&quot;, i.e. <code>permission1.implies(permission2)</code> ,
     * then any Subject granted {@code permission1} would have ability greater than or equal to that defined by
     * {@code permission2}.
     *
     * @param p the permission to check for behavior/functionality comparison.
     * @return {@code true} if this current instance <em>implies</em> all the functionality and/or resource access
     * described by the specified {@code Permission} argument, {@code false} otherwise.
     */
    @Override
    public boolean implies(Permission p) {

        // By default only supports comparisons with other WildcardPermissions
        if (!(p instanceof IOTPermission)) {
            return false;
        }

        IOTPermission otherP = (IOTPermission) p;

        if(!getType().equals(otherP.getType())){

            if (PUBLISH_SUBSCRIBE_ROLE.equals(getType())
                    && (PUBLISH_ROLE.equals(otherP.getType())
                    || SUBSCRIBE_ROLE.equals(otherP.getType()))
                    || PUBLISH_SUBSCRIBE_ROLE.equals(otherP.getType())
                    && (PUBLISH_ROLE.equals(getType())
                    || SUBSCRIBE_ROLE.equals(getType()))) {

                //This is an allowed situation.
            }else

            return false;

        }


        List<String> otherParts = otherP.getParts();

        int i = 0;
        for (String otherPart : otherParts) {
            // If this permission has less parts than the other permission,
            // everything after the number of parts contained
            // in this permission is automatically implied, so return true
            if (getParts().size() - 1 < i) {
                return true;
            } else {
                String part = getParts().get(i);

                if (!Objects.equals(part, otherPart)) {

                    //Check username
                    switch (part) {
                        case USERNAME_TOKEN:
                            if (!Objects.equals(otherP.getUsername(), otherPart))
                                return false;
                            break;
                        case PARTITION_TOKEN:
                            if (!Objects.equals(otherP.getPartition(), otherPart))
                                return false;
                            break;
                        case CLIENT_ID_TOKEN:

                            if (!Objects.equals(otherP.getClientId(), otherPart))
                                return false;
                            break;
                        default:
                            if (!( Objects.equals(MULTI_LEVEL_WILDCARD_TOKEN, part)
                                    || Objects.equals(SINGLE_LEVEL_WILDCARD_TOKEN, part)))
                                return false;
                    }
                }
            }
            i++;
        }


        // If this permission has more parts than the other parts,
        // only imply it if all of the other parts are wildcards
        for (; i < getParts().size(); i++) {
            String part = getParts().get(i);
            if (!MULTI_LEVEL_WILDCARD_TOKEN.equals(part)) {
                return false;
            }
        }

        return true;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();

           for (String part : parts) {
            if(buffer.length()==0){
                buffer.append(getType()).append(":");
            }else {
                buffer.append("/");
            }
                buffer.append(part);
        }
        return buffer.toString();
    }

    public boolean equals(Object o) {
        if (o instanceof IOTPermission) {
            IOTPermission clp = (IOTPermission) o;
            return parts.equals(clp.parts);
        }
        return false;
    }

    public int hashCode() {
        return parts.hashCode();
    }

}
