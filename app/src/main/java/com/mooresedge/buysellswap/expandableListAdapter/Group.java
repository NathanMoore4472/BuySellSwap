package com.mooresedge.buysellswap.expandableListAdapter;

/**
 * Created by Nathan on 29/05/2017.
 */

import java.util.List;

public class Group {

    private String Name;
    private List<String> Children;
    public Group(String name, List<String> children) {
        super();
        Name = name;
        Children = children;
    }
    public String getName() {
        return Name;
    }
    public void setName(String name) {
        Name = name;
    }
    public List<String> getChildren() {
        return Children;
    }
    public void setChildren(List<String> children) {
        Children = children;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((Children == null) ? 0 : Children.hashCode());
        result = prime * result + ((Name == null) ? 0 : Name.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Group other = (Group) obj;
        if (Children == null) {
            if (other.Children != null)
                return false;
        } else if (!Children.equals(other.Children))
            return false;
        if (Name == null) {
            if (other.Name != null)
                return false;
        } else if (!Name.equals(other.Name))
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "Group [Name=" + Name + ", Children=" + Children + "]";
    }
}