package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.io.EOFException;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see	nachos.vm.VMProcess
 * @see	nachos.network.NetProcess
 */
public class UserProcess {
    /**
     * Allocate a new process.
     */
    public UserProcess() {
    //System.out.println(nextPid);
	int numPhysPages = Machine.processor().getNumPhysPages();
    pageTable = new TranslationEntry[numPhysPages];
    descriptorClass = new DescriptorClass();
    descriptorClass.add(UserKernel.console.openForReading());
    descriptorClass.add(UserKernel.console.openForWriting());
	for (int i=0; i<numPhysPages; i++)
	    pageTable[i] = new TranslationEntry(i,i, true,false,false,false);
    
    pid = nextPid;
    nextPid += 1;
    activeProcess += 1;
    childProcess = new LinkedList<UserProcess>();
    isDone = new Semaphore(0);
    status = -1;
    }
    /**
     * Allocate and return a new process of the correct class. The class name
     * is specified by the <tt>nachos.conf</tt> key
     * <tt>Kernel.processClassName</tt>.
     *
     * @return	a new process of the correct class.
     */
    public static UserProcess newUserProcess() {
	return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
    }

    /**
     * Execute the specified program with the specified arguments. Attempts to
     * load the program, and then forks a thread to run it.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the program was successfully executed.
     */
    public boolean execute(String name, String[] args) {
	if (!load(name, args))
	    return false;
    //System.out.println("!!!");
	new UThread(this).setName(name).fork();
    //System.out.println("???");
	return true;
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
	Machine.processor().setPageTable(pageTable);
    }

    /**
     * Read a null-terminated string from this process's virtual memory. Read
     * at most <tt>maxLength + 1</tt> bytes from the specified address, search
     * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
     * without including the null terminator. If no null terminator is found,
     * returns <tt>null</tt>.
     *
     * @param	vaddr	the starting virtual address of the null-terminated
     *			string.
     * @param	maxLength	the maximum number of characters in the string,
     *				not including the null terminator.
     * @return	the string read, or <tt>null</tt> if no null terminator was
     *		found.
     */
    public String readVirtualMemoryString(int vaddr, int maxLength) {
	Lib.assertTrue(maxLength >= 0);

	byte[] bytes = new byte[maxLength+1];

	int bytesRead = readVirtualMemory(vaddr, bytes);

	for (int length=0; length<bytesRead; length++) {
	    if (bytes[length] == 0)
		return new String(bytes, 0, length);
	}

	return null;
    }

    /**
     * Transfer data from this process's virtual memory to all of the specified
     * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data) {
	return readVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from this process's virtual memory to the specified array.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @param	offset	the first byte to write in the array.
     * @param	length	the number of bytes to transfer from virtual memory to
     *			the array.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data, int offset,
				 int length) {
    Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);
    byte[] memory = Machine.processor().getMemory(); 
    
    length = (length > (pageSize*numPages - vaddr)) ? pageSize*numPages - vaddr : length;
    length = (data.length - offset < length) ? data.length - offset : length;

    int transferred = 0;
    do{
        int pNum = Processor.pageFromAddress(vaddr + transferred), pOffset = Processor.offsetFromAddress(vaddr + transferred);;
        /*if (pageNum<0 || pageNum>=pageTable.length)
            return 0; */
        int leftByte = pageSize-pOffset;
        int amount = (leftByte < length - transferred) ? leftByte : (length - transferred);
        int pAddr = pageTable[pNum].ppn*pageSize + pOffset;
        System.arraycopy(memory, pAddr, data, offset + transferred, amount);
        transferred += amount;
    }while(transferred < length);

    return transferred;
    }

    /**
     * Transfer all data from the specified array to this process's virtual
     * memory.
     * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data) {
	return writeVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from the specified array to this process's virtual memory.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @param	offset	the first byte to transfer from the array.
     * @param	length	the number of bytes to transfer from the array to
     *			virtual memory.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data, int offset,
				  int length) {
    Lib.assertTrue(offset >= 0 && length >= 0 && offset + length <= data.length);
    byte[] memory = Machine.processor().getMemory(); 
    
    length = (length > (pageSize*numPages - vaddr)) ? pageSize*numPages - vaddr : length;
    length = (data.length - offset < length) ? data.length - offset : length;

    int transferred = 0;
    do{
        int pNum = Processor.pageFromAddress(vaddr + transferred), pOffset = Processor.offsetFromAddress(vaddr + transferred);;
        /*if (pageNum<0 || pageNum>=pageTable.length)
            return 0; */
        int leftByte = pageSize-pOffset;
        int amount = (leftByte < length - transferred) ? leftByte : (length - transferred);
        int pAddr = pageTable[pNum].ppn*pageSize + pOffset;
        System.arraycopy(data, offset + transferred, memory, pAddr, amount);
        transferred += amount;
    }while(transferred < length);

    return transferred;
    }

    /**
     * Load the executable with the specified name into this process, and
     * prepare to pass it the specified arguments. Opens the executable, reads
     * its header information, and copies sections and arguments into this
     * process's virtual memory.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the executable was successfully loaded.
     */
    private boolean load(String name, String[] args) {
	Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");
	
	OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
	if (executable == null) {
	    Lib.debug(dbgProcess, "\topen failed");
	    return false;
	}

	try {
	    coff = new Coff(executable);
	}
	catch (EOFException e) {
	    executable.close();
	    Lib.debug(dbgProcess, "\tcoff load failed");
	    return false;
	}

	// make sure the sections are contiguous and start at page 0
	numPages = 0;
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
	    if (section.getFirstVPN() != numPages) {
		coff.close();
		Lib.debug(dbgProcess, "\tfragmented executable");
		return false;
	    }
	    numPages += section.getLength();
	}

	// make sure the argv array will fit in one page
	byte[][] argv = new byte[args.length][];
	int argsSize = 0;
	for (int i=0; i<args.length; i++) {
	    argv[i] = args[i].getBytes();
	    // 4 bytes for argv[] pointer; then string plus one for null byte
	    argsSize += 4 + argv[i].length + 1;
	}
	if (argsSize > pageSize) {
	    coff.close();
	    Lib.debug(dbgProcess, "\targuments too long");
	    return false;
	}

	// program counter initially points at the program entry point
	initialPC = coff.getEntryPoint();	

	// next comes the stack; stack pointer initially points to top of it
	numPages += stackPages;
	initialSP = numPages*pageSize;

	// and finally reserve 1 page for arguments
	numPages++;

	if (!loadSections())
	    return false;

	// store arguments in last page
	int entryOffset = (numPages-1)*pageSize;
	int stringOffset = entryOffset + args.length*4;

	this.argc = args.length;
	this.argv = entryOffset;
	
	for (int i=0; i<argv.length; i++) {
	    byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
	    Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);
	    entryOffset += 4;
	    Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
		       argv[i].length);
	    stringOffset += argv[i].length;
	    Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
	    stringOffset += 1;
	}

	return true;
    }

    /**
     * Allocates memory for this process, and loads the COFF sections into
     * memory. If this returns successfully, the process will definitely be
     * run (this is the last step in process initialization that can fail).
     *
     * @return	<tt>true</tt> if the sections were successfully loaded.
     */
    protected boolean loadSections() {
    
    int [] idx = UserKernel.allocatePP(numPages);
    if (idx == null) {
        // No enough free pages
	    coff.close();
        return false;
    }

    pageTable = new TranslationEntry[numPages];

	// load sections
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
	    
	    Lib.debug(dbgProcess, "\tinitializing " + section.getName()
		      + " section (" + section.getLength() + " pages)");

	    for (int i = 0; i < section.getLength(); i++) {
		    int vpn = section.getFirstVPN()+i;
            int ppn = idx[vpn];
            pageTable[vpn] = new TranslationEntry(vpn, ppn, true, section.isReadOnly(), false, false);
		    // for now, just assume virtual addresses=physical addresses
		    section.loadPage(i, ppn);
	    }
    }
    // allocate free pages for stack and argv
    
	for (int i = numPages - stackPages - 1; i < numPages; i++) 
		pageTable[i] = new TranslationEntry(i, idx[i], true, false, false, false);
	
	return true;
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
        coff.close();
		for (int i = 0; i < numPages; i++)
			UserKernel.releasePP(pageTable[i].ppn);
		pageTable = null;
    }    

    /**
     * Initialize the processor's registers in preparation for running the
     * program loaded into this process. Set the PC register to point at the
     * start function, set the stack pointer register to point at the top of
     * the stack, set the A0 and A1 registers to argc and argv, respectively,
     * and initialize all other registers to 0.
     */
    public void initRegisters() {
	Processor processor = Machine.processor();

	// by default, everything's 0
	for (int i=0; i<processor.numUserRegisters; i++)
	    processor.writeRegister(i, 0);

	// initialize PC and SP according
	processor.writeRegister(Processor.regPC, initialPC);
	processor.writeRegister(Processor.regSP, initialSP);

	// initialize the first two argument registers to argc and argv
	processor.writeRegister(Processor.regA0, argc);
	processor.writeRegister(Processor.regA1, argv);
    }

    /**
     * Handle the halt() system call. 
     */
    private int handleHalt() {
    if (this == UserKernel.root){
        Machine.halt();
    }
    else{
        // Ignore halt and return
        return -1;
    }
	Lib.assertNotReached("Machine.halt() did not halt machine!");
	return -1;
    }

    private int handleCreate(int fileAddr) {
        String name = readVirtualMemoryString(fileAddr, maxFilenameLen);

        if (name == null) return -1;
        
        if (fileToRm.contains(name)) return -1;
        
        OpenFile file = ThreadedKernel.fileSystem.open(name, true);

        if (file == null) {
			return -1;
		}
        return descriptorClass.add(file);
    }

    private int handleOpen(int fileAddr) {
        String name = readVirtualMemoryString(fileAddr, maxFilenameLen);

        if (name == null) return -1;
        
        if (fileToRm.contains(name)) return -1;
        
        OpenFile file = ThreadedKernel.fileSystem.open(name, false);

        if (file == null) {
			//Lib.debug(dbgProcess, "Invalid file name pointer");
			return -1;
        }
        
        return descriptorClass.add(file);
    }

    protected int handleRead(int descriptor, int bAddr, int len) {
		OpenFile file = descriptorClass.get(descriptor);

		if (file == null) {
			return -1;
        }
        
		if (bAddr < 0 || len < 0) {
			return -1;
		}

		byte tmp[] = new byte[len];
		int readOut = file.read(tmp, 0, len);

		if (readOut == -1) {
			return -1;
		}

		int writeIn = writeVirtualMemory(bAddr, tmp, 0, readOut);

		return writeIn;
    }
    
    protected int handleWrite(int descriptor, int bAddr, int len) {
		OpenFile file = descriptorClass.get(descriptor);

		if (file == null) {
			return -1;
        }
        
        if (bAddr < 0 || len < 0) {
			return -1;
		}

        byte tmp[] = new byte[len];

		int readOut = readVirtualMemory(bAddr, tmp, 0, len);

        if (readOut == -1) {
			return -1;
        }
        
		int writeIn = file.write(tmp, 0, readOut);

		return writeIn;
    }
    
    protected int handleClose(int descriptor) {
		return descriptorClass.close(descriptor);
	}

    protected int handleUnlink(int fileAddr) {
		String name = readVirtualMemoryString(fileAddr, maxFilenameLen);

		if (name == null) return -1;
        
		if (filesAndNum.containsKey(name)) fileToRm.add(name);
		else if(ThreadedKernel.fileSystem.remove(name))
            return 0;
        return -1;
    }
    
    protected int handleExec(int fileAddr, int argNum, int argAddr) {
        String name = readVirtualMemoryString(fileAddr, maxFilenameLen);
        if(name==null || argNum < 0 || argAddr < 0 || argAddr > numPages*pageSize || !name.endsWith(".coff"))
            return -1;
        String[] args = new String[argNum];
        byte[] tmp = new byte[4];

        for (int i = 0, readOut; i < argNum; i++) {
			readOut = readVirtualMemory(argAddr + i * 4, tmp);
			if (readOut != 4)	return -1;
			args[i] = readVirtualMemoryString(Lib.bytesToInt(tmp, 0), maxFilenameLen);
			if (args[i] == null)    return -1;
        }
        
        UserProcess cProcess = new UserProcess();
        cProcess.parentProcess = this;
        this.childProcess.add(cProcess);

        if (!cProcess.execute(name, args))  return -1;
        
        return cProcess.pid;
    }
    
    protected int handleJoin(int pid, int statusAddr) {
        UserProcess joinedProcess = null;
        // is child?
        for (int i = 0; i < childProcess.size(); i++)
            if (pid == childProcess.get(i).pid)
            {
               joinedProcess = childProcess.get(i);
               break;
            }
        if (joinedProcess == null)  return -1;

        // Wait for child finished
		joinedProcess.isDone.P();

		writeVirtualMemory(statusAddr, Lib.bytesFromInt(joinedProcess.status));

		if (joinedProcess.normalExit)
			return 1;
		else
			return 0;
    }
    
    protected int handleExit(int status) {

		this.status = status;
		
		for (int i = 2; i < maxDescriptorNum; i++)
			descriptorClass.close(i);

        if(parentProcess != null) parentProcess.childProcess.remove(this);
        unloadSections();
		isDone.V();

        activeProcess -= 1;
		if (activeProcess == 0)
			Kernel.kernel.terminate();

		UThread.finish();

		return 0;
	}

    public class DescriptorClass {
		public DescriptorClass(){
            descriSet = new OpenFile[maxDescriptorNum];
        }

		public int add(OpenFile file) {
			for (int i = 0; i < maxDescriptorNum; i++)
				if (descriSet[i] == null){
                    //return add(i, file);
                    descriSet[i] = file;
                    if (filesAndNum.get(file.getName()) != null) {
                        // File is already in the table
                        filesAndNum.put(file.getName(), filesAndNum.get(file.getName())+1);
                    }
                    else {
                        filesAndNum.put(file.getName(), 1);
                    }
                    return i;
                }

			return -1;
		}

		public int close(int descriptor) {
			if (descriSet[descriptor] == null) return -1;

			OpenFile file = descriSet[descriptor];
			descriSet[descriptor] = null;
			file.close();

			if (filesAndNum.get(file.getName()) > 1)
				filesAndNum.put(file.getName(), filesAndNum.get(file.getName()) - 1);
			else {
                filesAndNum.remove(file.getName());
                // Delte it in this stage
				if (fileToRm.contains(file.getName())) {
					fileToRm.remove(file.getName());
					ThreadedKernel.fileSystem.remove(file.getName());
				}
			}
            
			return 0;
		}

		public OpenFile get(int descriptor) {
			if (descriptor < 0 || descriptor >= maxDescriptorNum)   return null;
			return descriSet[descriptor];
        }

        private OpenFile descriSet[];
	}

    private static final int
    syscallHalt = 0,
	syscallExit = 1,
	syscallExec = 2,
	syscallJoin = 3,
	syscallCreate = 4,
	syscallOpen = 5,
	syscallRead = 6,
	syscallWrite = 7,
	syscallClose = 8,
	syscallUnlink = 9;

    /**
     * Handle a syscall exception. Called by <tt>handleException()</tt>. The
     * <i>syscall</i> argument identifies which syscall the user executed:
     *
     * <table>
     * <tr><td>syscall#</td><td>syscall prototype</td></tr>
     * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
     * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
     * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
     * 								</tt></td></tr>
     * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
     * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
     * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
     * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
     * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
     * </table>
     * 
     * @param	syscall	the syscall number.
     * @param	a0	the first syscall argument.
     * @param	a1	the second syscall argument.
     * @param	a2	the third syscall argument.
     * @param	a3	the fourth syscall argument.
     * @return	the value to be returned to the user.
     */
    public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
    //System.out.println("AAAAAAAAAAAAAA");
	switch (syscall) {
	case syscallHalt:
        return handleHalt();
    case syscallCreate:
        return handleCreate(a0);
    case syscallOpen:
        return handleOpen(a0);
    case syscallRead:
        return handleRead(a0,a1,a2);
    case syscallWrite:
        return handleWrite(a0,a1,a2);
    case syscallClose:
        return handleClose(a0);
    case syscallUnlink:
        return handleUnlink(a0);
    case syscallExec:
        return handleExec(a0, a1, a2);
    case syscallJoin:
        return handleJoin(a0, a1);
    case syscallExit:
        return handleExit(a0);
    default:
        normalExit = false;
        handleExit(-1);
        //return -1;
	}
	return 0;
    }

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param	cause	the user exception that occurred.
     */
    public void handleException(int cause) {
	Processor processor = Machine.processor();

	switch (cause) {
	case Processor.exceptionSyscall:
	    int result = handleSyscall(processor.readRegister(Processor.regV0),
				       processor.readRegister(Processor.regA0),
				       processor.readRegister(Processor.regA1),
				       processor.readRegister(Processor.regA2),
				       processor.readRegister(Processor.regA3)
				       );
	    processor.writeRegister(Processor.regV0, result);
	    processor.advancePC();
	    break;				       
				       
	default:
	    Lib.debug(dbgProcess, "Unexpected exception: " +
		      Processor.exceptionNames[cause]);
	    Lib.assertNotReached("Unexpected exception");
	}
    }
    public UserProcess parentProcess = null;
    public int pid;
    public int status;
    public Semaphore isDone;
    public LinkedList<UserProcess> childProcess;
    public static LinkedList<String> fileToRm = new LinkedList<String>();
    protected static Hashtable<String, Integer> filesAndNum = new Hashtable<String, Integer>();
    /** The program being run by this process. */
    protected Coff coff;
    protected boolean normalExit = true;
    /** This process's page table. */
    protected TranslationEntry[] pageTable;
    /** The number of contiguous pages occupied by the program. */
    protected int numPages;

    /** The number of pages in the program's stack. */
    protected final int stackPages = 8;
    
    protected final int maxDescriptorNum = 16;
    protected final int maxFilenameLen = 256;

    private int initialPC, initialSP;
    private int argc, argv;
    private DescriptorClass descriptorClass;
    
    
    

    private static int nextPid = 0;
    private static int activeProcess = 0;
    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
}
