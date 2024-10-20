import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class SubleqConfig(
    val width: Int
) {
    def dtype() = SInt(width bits)
    def reg() = Reg(dtype()) init(0)
}


case class SubleqBus(cfg: SubleqConfig) extends Bundle {
    val addr = out(cfg.dtype())
    val rdata = in(cfg.dtype())
    val wdata = out(cfg.dtype())
    val write = out(Bool())
}


/*

def subleq(rd, wr):
    var a, b, c, r
    var ip = 0
    while(true):
        a = rd(ip++)
        b = rd(ip++)
        c = rd(ip++)
        r = rd(b) - rd(a)
        wr(b, r)
        if(r <= 0) ip = c

*/


case class Subleq(cfg: SubleqConfig) extends Component {
    val io = new Bundle {
        val bus = SubleqBus(cfg)
    }


    val regs = new Area {
        val a = cfg.reg()
        val b = cfg.reg()
        val c = cfg.reg()
        val ip = cfg.reg() init(0)
    }


    val b_addr = cfg.reg()
    val b_wdata = cfg.reg()
    val b_write = Reg(Bool()) init(False)

    io.bus.addr := b_addr

    when(b_write) {
        io.bus.wdata := b_wdata
    } otherwise {
        io.bus.wdata := 0
    }

    io.bus.write := b_write


    val t0 = cfg.reg()
    val t1 = cfg.reg()


    val nIp = regs.ip + 1


    val sub = t1 - t0
    val br = sub <= 0


    /*
    val fsm = new StateMachine {
        def fetchRegState(addr: SInt, reg: SInt, incIP: Boolean = true) = new State {
            onEntry {
                b_addr := addr
            }
            whenIsActive {
                reg := io.bus.rdata
            }
            onExit {
                if(incIP) regs.ip := nIp
            }
        }

        val fetchA = fetchRegState(regs.ip, regs.a)
        setEntry(fetchA)

        val fetchB = fetchRegState(regs.ip, regs.b)
        fetchA.whenIsActive(goto(fetchB))

        val fetchC = fetchRegState(regs.ip, regs.c)
        fetchB.whenIsActive(goto(fetchC))

        val fetchT0 = fetchRegState(regs.a, t0, false)
        fetchC.whenIsActive(goto(fetchT0))

        val fetchT1 = fetchRegState(regs.b, t1, false)
        fetchT0.whenIsActive(goto(fetchT1))

        val execute = new State {
            onEntry {
                b_addr := regs.b
                b_wdata := sub
                b_write := True
            }
            whenIsActive(goto(fetchA))
            onExit {
                b_write := False
                when(br) {
                    regs.ip := regs.c
                } otherwise {
                    regs.ip := nIp
                }
            }
        }
        fetchT1.whenIsActive(goto(execute))
    }
    */


    val state = Reg(UInt(4 bits)) init(0)
    
    var stateID = 0
    def nextStateID(): Int = {
        var id = stateID
        stateID += 1
        return id
    }

    switch(state) {
        def defFetchState(addr: SInt, reg: SInt, incIP: Boolean = true) {
            is(nextStateID()) {
                b_addr := addr
            }
            is(nextStateID()) {
                reg := io.bus.rdata
                if(incIP) regs.ip := nIp
            }
        }

        defFetchState(regs.ip, regs.a)
        defFetchState(regs.ip, regs.b)
        defFetchState(regs.ip, regs.c)
        defFetchState(regs.a, t0, false)
        defFetchState(regs.b, t1, false)

        is(nextStateID()) {
            b_addr := regs.b
            b_wdata := sub
            b_write := True
        }
        is(nextStateID()) {
            b_write := False
        }

        is(nextStateID()) {
            when(br) {
                regs.ip := regs.c
            }
        }
    }

    when(state === stateID) {
        state := 0
    } otherwise {
        state := state + 1
    }
}

