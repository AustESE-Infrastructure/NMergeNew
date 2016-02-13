/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.luc.nmerge.mvd.table;
import java.util.*;
/**
 * A section within a table
 */
public class Section
{
    HashMap<Short,FragList> lists;
    SectionState state;
    BitSet versions;
    int offset;
    Section()
    {
        lists = new HashMap<Short,FragList>();
        offset = -1;
        this.versions = new BitSet();
    }
    BitSet getVersions()
    {
        return versions;
    }
    /**
     * Get our starting point in base
     * @return the first base offset
     */
    int getOffset()
    {
        return offset;
    }
    /**
     * Add a fragment belonging to just one version
     * @param kind the fragment kind
     * @param base the version of the fragment
     * @param offset the offset in base where this frag starts
     * @param bs the set of versions sharing this frag
     * @param frag the actual fragment
     */
    void addFrag( FragKind kind, short base, int offset, BitSet bs, String frag )
    {
        FragList fl;
        this.versions.or(bs);
        if ( this.offset == -1 )
            this.offset = offset;
        this.state = SectionState.state(kind);
        if ( !lists.containsKey(base) )
        {
            fl = new FragList();
            lists.put(base,fl);
        }
        fl = lists.get(base);
        fl.add( kind, frag, bs );
    }
    /**
     * Add a frag that belongs to a set of versions
     * @param kind the fragment kind
     * @param bs the set of versions it belongs to
     * @param frag the text of the fragment
     */
    void addFragSet( FragKind kind, int offset, BitSet bs, String frag )
    {
        this.versions.or(bs);
        if ( this.offset == -1 )
            this.offset = offset;
        this.state = SectionState.state(kind);
        for (int i = bs.nextSetBit(1); i>= 0; 
            i = bs.nextSetBit(i+1))
        {
            if ( lists.containsKey((short)i) )
            {
                FragList fl = lists.get( (short)i );
                fl.add( kind, frag, bs );
            }
            else
            {
                FragList fl = new FragList();
                fl.add( kind, frag, bs );
                lists.put( (short)i, fl );
            }
        }
    }
    /**
     * Add a frag that belongs to an almost merged set of versions
     * @param bs the set of versions it belongs to
     * @param missing the versions it is missing in
     * @param frag the text of the fragment
     */
    void addAlmostSet( int offset, BitSet bs, BitSet missing, String frag )
    {
        this.versions.or(bs);
        if ( this.offset == -1 )
            this.offset = offset;
        this.state = SectionState.almost;
        for (int i = bs.nextSetBit(1); i>= 0; 
            i = bs.nextSetBit(i+1))
        {
            if ( lists.containsKey((short)i) )
            {
                FragList fl = lists.get( (short)i );
                fl.add( FragKind.almost, frag, bs );
            }
            else
            {
                FragList fl = new FragList();
                fl.add( FragKind.almost, frag, bs );
                lists.put( (short)i, fl );
            }
        }
        // fill in the gaps with empty fraglists
        for (int i = missing.nextSetBit(1); i>= 0; 
                i = missing.nextSetBit(i+1))
        {
            if ( !lists.containsKey((short)i) )
            {
                FragList fl = new FragList();
                fl.add( FragKind.almost, "", missing );
                lists.put( (short)i, fl );
            }
        }
    }
}