/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javasmpmasterslave;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;
/**
 *
 * @author Haley
 */
import java.util.logging.Level;
import java.util.logging.Logger;
public class JavaSMPMasterSlave {

    
    
    public static void main(String[] args) {
        // TODO code application logic here
        if (args.length != 1) {
            System.out.println("Usage:\njava JavaSMPMasterSlave <input_file>");
            return;
        }

        Scanner input;
        try {
            input = new Scanner(new File(args[0]));
        } catch (FileNotFoundException e) {
            System.out.printf("File \"%s\" not found.\n", args[0]);
            return;
        }
        
        int n = input.nextInt();

        int[][] man_preferences = new int[n][n];
        int[][] woman_preferences = new int[n][n];

        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                man_preferences[i][j] = input.nextInt() - 1;

        for(int i = 0; i < n; i++)
            for(int j = 0; j < n; j++)
                woman_preferences[i][j] = input.nextInt() - 1;
        
        Master matchmaker;
        matchmaker = new Master(woman_preferences, man_preferences);
        //int i = 0;
        do{
            matchmaker.proposal_round();
            //System.out.println("here");
            //matchmaker.print_matchings();
            //i++;
        //}while(i < 1);
        }while(!matchmaker.done);
        matchmaker.print_matchings();
    }
}

class Master {
	private final int slave_count;
	private final Resource res;
        private final Slave[] slaves;
        boolean done = false;
        
        public Master(int[][] m_prefs, int[][] w_prefs){
            this.slave_count = m_prefs.length;
            this.res = new Resource(m_prefs, w_prefs);
            this.slaves = new Slave[m_prefs.length];
        }
        
        public void proposal_round() {
            int[] proposals = new int[this.slave_count];
            System.arraycopy(this.res.get_all_prefs_at_current_request_position(), 0, proposals, 0, proposals.length);
            // create slaves:
            for(int i = 0; i < slave_count; i++) {
                int count = 0;
                for(int j = 0; j < proposals.length; j++){
                    if(proposals[j] == i){
                        count++;
                    }
                }
                if(count != 0){
                    int[] woman_suitors = new int[count];
                    int current_suitor_index = 0;
                    for(int j = 0; j < proposals.length; j++){
                        if(proposals[j] == i){
                            woman_suitors[current_suitor_index] = j;
                            current_suitor_index++;
                        }
                    }
                    slaves[i] = new Slave(this.res, woman_suitors, i, this.slave_count);
                }
                else{
                    slaves[i] = new Slave();
                }
            }
            // start slaves:
            for(int i = 0; i < slave_count; i++) {
                slaves[i].start();
            }
            // wait for slaves to die:
            for(int i = 0; i < slave_count; i++) {
                try {
                    slaves[i].join();
                } catch (InterruptedException ex) {
                    Logger.getLogger(JavaSMPMasterSlave.class.getName()).log(Level.SEVERE, null, ex);
                }
                //System.out.println(slaves[i].getName() + " has died");
            }
            this.done = this.res.check_done_status();
            //System.out.println(this.done);
            //System.out.println("The master will now die ... ");
        }
        
        public void print_matchings(){
            int[] partners = new int[this.slave_count];
            for(int i = 0; i < this.slave_count; i++){
                partners[this.res.get_current_partner(i)] = i;
            }
            for(int i = 0; i < partners.length; i++){
                System.out.println("(" + Integer.toString(i + 1) + "," + Integer.toString(partners[i] + 1) + ")");
            }
        }
    }
    
    class Slave extends Thread {

	private Resource sharedResource;
	private boolean done = false;
        private int[] proposals;
        private int woman_id;
        private int total_number_of_men;
        private boolean do_something = true;

	public void halt() {
            this.done = true;
	}

        public Slave(){
            this.do_something = false;
        }
        
	public Slave(Resource rcs, int[] proposals, int woman_id, int total_number_of_men) {
            this.sharedResource = rcs;
            this.proposals = new int[proposals.length];
            System.arraycopy(proposals, 0, this.proposals, 0, proposals.length);
            this.woman_id =  woman_id;
            this.total_number_of_men = total_number_of_men;
	}

	protected boolean task() {
            int[] prefs_list = new int[this.total_number_of_men];
            System.arraycopy(this.sharedResource.get_woman_pref_list(this.woman_id), 0, prefs_list, 0, prefs_list.length);
            int best_man_id = this.proposals[0];
            int best_pref_position = this.get_array_position(prefs_list, best_man_id);
            if(this.sharedResource.get_current_partner(this.woman_id) != -1){
                if(this.get_array_position(prefs_list, this.sharedResource.get_current_partner(this.woman_id)) < best_pref_position){
                    best_man_id = this.sharedResource.get_current_partner(this.woman_id);
                    best_pref_position = this.get_array_position(prefs_list, best_man_id);
                }
            }
            for(int i = 1; i < this.proposals.length; i++){
                int current_proposer = this.proposals[i];
                int current_position = this.get_array_position(prefs_list, current_proposer);
                if(current_position < best_pref_position){
                    best_man_id = current_proposer;
                    best_pref_position = current_position;
                }
            }
            if(best_man_id != this.sharedResource.get_current_partner(this.woman_id) && this.sharedResource.get_current_partner(this.woman_id) != -1){
                this.sharedResource.update_man_availability(this.sharedResource.get_current_partner(this.woman_id));
                this.sharedResource.update_man_availability(best_man_id);
                this.sharedResource.update_partner(best_man_id, this.woman_id);
            }
            else{
                this.sharedResource.update_man_availability(best_man_id);
                this.sharedResource.update_partner(best_man_id, woman_id);
            }
            return true;
	}

        public void run() {
            if(this.do_something){
                this.done = task();
            }
            else{
                this.done = true;
            }
        }
        
        private int get_array_position(int[] prefs_list, int id_to_find){
            for(int i = 0; i < prefs_list.length; i++){
                if(prefs_list[i] == id_to_find){
                    return i;
                }
            }
            //You should never return this
            return -1;
        }
    }
    
    class Resource {

	private int[][] man_prefs;
        private int[][] woman_prefs;
        private int[] current_partners;
        private boolean[] men_availability;
        private int[] current_man_request_position;

	public Resource(int[][] man_prefs, int[][] woman_prefs){
            this.man_prefs = new int[man_prefs.length][man_prefs[0].length];
            this.woman_prefs = new int[woman_prefs.length][woman_prefs[0].length];
            this.current_partners = new int[woman_prefs.length];
            Arrays.fill(this.current_partners, -1);
            for(int i = 0; i < man_prefs.length; i++){
                System.arraycopy(man_prefs[i], 0, this.man_prefs[i], 0, man_prefs[i].length);
            }
            for(int i = 0; i < woman_prefs.length; i++){
                System.arraycopy(woman_prefs[i], 0, this.woman_prefs[i], 0, woman_prefs[i].length);
            }
            this.men_availability = new boolean[man_prefs.length];
            Arrays.fill(this.men_availability, true);
            this.current_man_request_position = new int[man_prefs.length];
            Arrays.fill(this.current_man_request_position, 0);
        }
        
        public int[] get_all_prefs_at_current_request_position(){
            int[] array_to_return = new int[this.man_prefs.length];
            for(int i = 0; i < array_to_return.length; i++){
                if(this.men_availability[i]){
                    array_to_return[i] = this.man_prefs[i][this.current_man_request_position[i]];
                    this.increase_current_request_position(i);
                }
                else{
                    array_to_return[i] = -1;
                }
            }
            return array_to_return;
        }
        
        public void increase_current_request_position(int man_id){
            this.current_man_request_position[man_id]++;
        }
        
        public synchronized void update_partner(int man_id, int woman_id){
            this.current_partners[woman_id] = man_id;
        }
        
        public int get_current_partner(int woman_id){
            return this.current_partners[woman_id];
        }
        
        public int[] get_woman_pref_list(int woman_id){
            int[] array_to_return = new int[this.woman_prefs[woman_id].length];
            System.arraycopy(this.woman_prefs[woman_id], 0, array_to_return, 0, array_to_return.length);
            return array_to_return;
        }
        
        public synchronized void update_man_availability(int man_to_update){
            this.men_availability[man_to_update] = !this.men_availability[man_to_update];
        }
        
        public boolean check_done_status(){
            for(int i = 0; i < this.men_availability.length; i++){
                //System.out.println(this.men_availability.length);
                //System.out.println(this.men_availability[i]);
                if(this.men_availability[i]){
                    return false;
                }
            }
            return true;
        }
        
    }
