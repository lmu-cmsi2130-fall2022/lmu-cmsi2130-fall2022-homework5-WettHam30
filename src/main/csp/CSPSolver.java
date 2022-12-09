package main.csp;

import java.time.LocalDate;
import java.util.*;

/**
 * CSP: Calendar Satisfaction Problem Solver
 * Provides a solution for scheduling some n meetings in a given
 * period of time and according to some unary and binary constraints
 * on the dates of each meeting.
 */
public class CSPSolver {

    // Backtracking CSP Solver
    // --------------------------------------------------------------------------------------------------------------
    
    /**
     * Public interface for the CSP solver in which the number of meetings,
     * range of allowable dates for each meeting, and constraints on meeting
     * times are specified.
     * @param nMeetings The number of meetings that must be scheduled, indexed from 0 to n-1
     * @param rangeStart The start date (inclusive) of the domains of each of the n meeting-variables
     * @param rangeEnd The end date (inclusive) of the domains of each of the n meeting-variables
     * @param constraints Date constraints on the meeting times (unary and binary for this assignment)
     * @return A list of dates that satisfies each of the constraints for each of the n meetings,
     *         indexed by the variable they satisfy, or null if no solution exists.
     */
    public static List<LocalDate> solve (int nMeetings, LocalDate rangeStart, LocalDate rangeEnd, Set<DateConstraint> constraints) {
        // [!] TODO!
    	List<LocalDate> ans = new ArrayList<LocalDate>();
    	List<MeetingDomain> domains = new ArrayList<MeetingDomain>();
    	
    	for(int i = 0; i < nMeetings; i++) {
    		domains.add(new MeetingDomain(rangeStart, rangeEnd));
    	}
    	
    	nodeConsistency(domains, constraints);
//    	arcConsistency(domains, constraints);
    	    	
    	solve(nMeetings, domains, constraints, ans);
    	
    	System.out.println(ans);
    	
    	if(ans.size() == 0) {
    		return null;
    	}
    	
    	return ans;
    }
    
    
    // Filtering Operations
    // --------------------------------------------------------------------------------------------------------------
    
    /**
     * Enforces node consistency for all variables' domains given in varDomains based on
     * the given constraints. Meetings' domains correspond to their index in the varDomains List.
     * @param varDomains List of MeetingDomains in which index i corresponds to D_i
     * @param constraints Set of DateConstraints specifying how the domains should be constrained.
     * [!] Note, these may be either unary or binary constraints, but this method should only process
     *     the *unary* constraints! 
     */
    public static void nodeConsistency (List<MeetingDomain> varDomains, Set<DateConstraint> constraints) {
        // [!] TODO!
    	
    	for (int i = 0; i < varDomains.size(); i++) {
    	    MeetingDomain newDom = varDomains.get(i);

    	    Iterator<LocalDate> iterator = newDom.domainValues.iterator();
    	    while (iterator.hasNext()) {
    	        LocalDate date = iterator.next();

    	        for (DateConstraint constraint : constraints) {

    	            if (constraint.arity() == 1) {
    	                UnaryDateConstraint c = (UnaryDateConstraint) constraint;
    	                
    	                if(c.L_VAL == i) {
        	                if (!constraint.isSatisfiedBy(date, c.R_VAL)) {
        	                    iterator.remove();
        	                }
    	                }
    	                
    	            }
    	        }
    	    }
    	}
    	
    	System.out.println(varDomains);
    }
    
    /**
     * Enforces arc consistency for all variables' domains given in varDomains based on
     * the given constraints. Meetings' domains correspond to their index in the varDomains List.
     * @param varDomains List of MeetingDomains in which index i corresponds to D_i
     * @param constraints Set of DateConstraints specifying how the domains should be constrained.
     * [!] Note, these may be either unary or binary constraints, but this method should only process
     *     the *binary* constraints using the AC-3 algorithm! 
     */
    public static void arcConsistency (List<MeetingDomain> varDomains, Set<DateConstraint> constraints) {
        // [!] TODO!
    	Set<Arc> arcSet = new HashSet<Arc>();
    	for(int i = 0; i < varDomains.size(); i++) {
    		for(DateConstraint d : constraints) { 
				BinaryDateConstraint constraint = (BinaryDateConstraint) d;
				
        		arcSet.add(new Arc(i, d.L_VAL, d));
        		arcSet.add(new Arc(i, constraint.L_VAL, constraint.getReverse()));
    		}
    	}
    	
    	int i = 0;
    	
    	while(arcSet.size() > 0) {
    		MeetingDomain a = varDomains.get(arcSet.stream().findFirst().get().TAIL);
    		arcSet.remove(arcSet.stream().findFirst().get());
    		MeetingDomain b = varDomains.get(arcSet.stream().findFirst().get().TAIL);
    		arcSet.remove(arcSet.stream().findFirst().get());

    		if(removeIncVal(a, b, constraints)) {
    			
    			for(DateConstraint d : constraints) {
    				
    				if(d.arity() == 1) {
    					arcSet.add(new Arc(i, d.L_VAL, d));
    				}
    			}
    		}
    		i++;
    	}
    	System.out.println(varDomains);
    }
    
    /**
     * Private helper class organizing Arcs as defined by the AC-3 algorithm, useful for implementing the
     * arcConsistency method.
     * [!] You may modify this class however you'd like, its basis is just a suggestion that will indeed work.
     */
    private static class Arc {
        
        public final DateConstraint CONSTRAINT;
        public final int TAIL, HEAD;
        
        /**
         * Constructs a new Arc (tail -> head) where head and tail are the meeting indexes
         * corresponding with Meeting variables and their associated domains.
         * @param tail Meeting index of the tail
         * @param head Meeting index of the head
         * @param c Constraint represented by this Arc.
         * [!] WARNING: A DateConstraint's isSatisfiedBy method is parameterized as:
         * isSatisfiedBy (LocalDate leftDate, LocalDate rightDate), meaning L_VAL for the first
         * parameter and R_VAL for the second. Be careful with this when creating Arcs that reverse
         * direction. You may find the BinaryDateConstraint's getReverse method useful here.
         */
        public Arc (int tail, int head, DateConstraint c) {
            this.TAIL = tail;
            this.HEAD = head;
            this.CONSTRAINT = c;
        }
        
        @Override
        public boolean equals (Object other) {
            if (this == other) { return true; }
            if (this.getClass() != other.getClass()) { return false; }
            Arc otherArc = (Arc) other;
            return this.TAIL == otherArc.TAIL && this.HEAD == otherArc.HEAD && this.CONSTRAINT.equals(otherArc.CONSTRAINT);
        }
        
        @Override
        public int hashCode () {
            return Objects.hash(this.TAIL, this.HEAD, this.CONSTRAINT);
        }
        
        @Override
        public String toString () {
            return "(" + this.TAIL + " -> " + this.HEAD + ")";
        }
        
    }
    
    private static List<LocalDate> solve (int nMeetings, List<MeetingDomain> domains,
    		Set<DateConstraint> constraints, List<LocalDate> ans) {   	
    	
    	if(nMeetings == ans.size()) {
    		return ans;
    	}
    	    	
    	MeetingDomain mDomain = domains.get(ans.size());
    	
		//for each date in current domain
		for(LocalDate m : mDomain.domainValues) {
    		ans.add(m);

			if(consistent(constraints, ans, m)) {

				List<LocalDate> result = solve(nMeetings, domains, constraints, ans);
				
				if(result == null || result.size() != 0) {
					return result;
				}
				    				
			}
			ans.remove(ans.lastIndexOf(m));			

		}
		
    	return null;   	
    }
    
    private static boolean consistent(Set<DateConstraint> constraints, List<LocalDate> ans, LocalDate date) {
		//if constraint violated, not consistent
		for(DateConstraint d : constraints) {
			
			if(d.L_VAL > ans.size() ) {
				continue;
			}
			
			for(int i = 0; i < ans.size(); i++) {
				if(d.arity() == 1) {

					UnaryDateConstraint constraint = (UnaryDateConstraint) d;
					//currently comparing to itself, get rVal
	    			if(!d.isSatisfiedBy(date, constraint.R_VAL)) {

	    				return false;
	    			}
				}

				if(d.arity() == 2) {
					BinaryDateConstraint constraint = (BinaryDateConstraint) d;
					
					if(constraint.L_VAL >= ans.size()|| constraint.R_VAL >= ans.size()) {
						continue;
					}
										
					if(!d.isSatisfiedBy(ans.get(constraint.L_VAL), ans.get(constraint.R_VAL))) {
						return false;
					}
				}
			}		
	
		}
		
		return true;
    }
    
    private static boolean removeIncVal(MeetingDomain tail, MeetingDomain head, Set<DateConstraint> constraints) {
    	boolean removed = false;
    	
    	for(LocalDate date : tail.domainValues) {
        	boolean consistent = false;

    		for(LocalDate date2 : head.domainValues) {
    			
    			for(DateConstraint d : constraints) {
    				
    				if(d.isSatisfiedBy(date2, date)) {
    					consistent = true;
    					break;
    				}
    			}
    		}
    		
        	if(!consistent) {
        		//remove tail date from domain
        		
        		tail.domainValues.remove(date);
        		
        		removed = true;
        	}

    	}
    	
    	return removed;
    }
}
