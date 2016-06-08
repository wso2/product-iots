package org.wso2.agent.resourcemonitor.systembridge;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.wso2.agent.resourcemonitor.exception.OperationalException;

public class BashRunner {

	private String executeScript(String[] script) throws Exception {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(script);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			boolean nextline=false;
			while ((line = reader.readLine()) != null) {
				if(nextline)
					output.append("\n");
				output.append(line);
				nextline=true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return output.toString().trim();

	}

	public String runScript(String os, int scriptid,String suPassword) throws Exception {
		String[] script = this.getScript(os, scriptid,suPassword);
		String output = this.executeScript(script);
		//System.out.println(output);
		return output;
	}

	private String[] getScript(String os, int scriptid,String suPassword) throws OperationalException {
		String script[] = null;
		try {
			if (scriptid == 0)
				script = new String[]{ "/bin/sh", "-c", "ifconfig -a | awk '/^[a-z]/ { iface=$1; mac=$NF; next } /inet addr:/ { print mac }' | grep -o -E '([[:xdigit:]]{1,2}:){5}[[:xdigit:]]{1,2}'"+" | tr '\n' ' '"};
			if (scriptid == 1)
				script = new String[]{ "/bin/sh", "-c", "cat /proc/cpuinfo |grep 'model name' | uniq | awk '{print substr($0, index($0,$4))}'"+" | tr '\n' ' '"};
			if(scriptid==2)
				script = new String[]{ "/bin/sh", "-c", "cat /proc/cpuinfo |grep 'cpu cores' | awk '{print substr($0, index($0,$4))}' | xargs  | sed -e 's/\\ /+/g' | bc"+" | tr '\n' ' '"};
			if(scriptid==3)
				script = new String[]{ "/bin/sh", "-c", "echo "+suPassword+" | sudo -S dmidecode -t 17 | grep 'Size.*MB' | awk '{s+=$2} END {print s / 1024}'"+" | tr '\n' ' '"};
			if(scriptid==4)
//				script = new String[]{ "/bin/sh", "-c", "echo Amilapwd1 | sudo -S hdparm -I /dev/sda |grep 'device size with M = 1000\\*1000' | awk '{print substr($0, index($0,$9))}' | awk -F'[\t(]' '{print $2}' | awk '{print substr($1,0)}'"+" | tr '\n' ' '"};
                script = new String[]{ "/bin/sh", "-c", "echo "+suPassword+" | sudo -S fdisk -l | grep 'Disk /dev/sda' | awk '{print $3}'"+" | tr '\n' ' '"};
			if(scriptid==5)
				script = new String[]{ "/bin/sh", "-c", "echo "+suPassword+" | sudo -S hdparm -I /dev/sda |grep 'device size with M = 1000\\*1000' | awk '{print $0}' | awk -F'[\t(]' '{print $2}' | awk '{print substr($1,0)}'"+" | tr '\n' ' '"};
			if(scriptid==10)
				script = new String[]{ "/bin/sh", "-c", "top -b -n2 | grep 'Cpu(s)' | tail -n 1 | awk '{print $2 + $4}'"+" | tr '\n' ' '"};
			if(scriptid==11)
				script = new String[]{ "/bin/sh", "-c", "free | grep Mem | awk '{print $3/$2 * 100.0}'"+" | tr '\n' ' '"};
			if(scriptid==12)
				script = new String[]{ "/bin/sh", "-c", "df -h | grep -vE '^Filesystem' | awk '{ print $1\" \"$3\" \"$2 }' | grep \"/\" | awk 'NF{sum1+=$2} NF{sum2+=$3} END {print (sum1/sum2)*100}'"+" | tr '\n' ' '"};
			if(scriptid==13)
				script = new String[]{ "/bin/sh", "-c", "upower -i $(upower -e | grep BAT) | grep  -E 'percentage' | awk '{ print $2 }' | tr '%' ' '"+" | tr '\n' ' '"};
			if(scriptid==14)
				script = new String[]{ "/bin/sh", "-c", "upower -i $(upower -e | grep BAT) | grep  -E 'state' | awk '{ if($2==\"charging\"||$2==\"fully-charged\") print 1; else print 0; }'"+" | tr '\n' ' '"};
		} catch (Exception e) {

		}
		if (script == null || script[2].isEmpty()) {
			throw new OperationalException("Script Not Found");
		}
		return script;
	}

}
