import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.lib.fsm._


case class SubleqConfig(
    val width: Int
) {
    def dtype() = UInt(width bits)
    def reg() = Reg(dtype()) init(0)
}


case class SubleqBusCmd(cfg: SubleqConfig) extends Bundle {
    import cfg._
    
    val addr  = dtype()
    val data  = dtype()
    val write = Bool()
}


case class SubleqBusRsp(cfg: SubleqConfig) extends Bundle with IMasterSlave {
    import cfg._

    val data  = dtype()

    def asMaster(): Unit = {
        out(data)
    }
}

case class SubleqBus(cfg: SubleqConfig) extends Bundle with IMasterSlave {
    val cmd = Stream(SubleqBusCmd(cfg))
    val rsp = SubleqBusRsp(cfg)

    def asMaster(): Unit = {
        master(cmd)
        slave(rsp)
    }
}


case class Subleq(cfg: SubleqConfig) extends Component {
    import cfg._

    val io = new Bundle {
        val bus                     = master(SubleqBus(cfg))
    }
    import io.bus._


    cmd.addr                        := 0
    cmd.data                        := 0
    cmd.write                       := False
    cmd.valid.setAsReg() init(False)
    

    val a                           = reg()
    val b                           = reg()
    val c                           = reg()
    val ip                          = reg()
    val t0                          = reg()
    val t1                          = reg()

    val sub                         = t1 - t0
    val br                          = sub <= 0


    val fsm                         = new StateMachine {
        val fetchA                  = new State with EntryPoint
        val fetchB                  = new State
        val fetchC                  = new State
        val fetchT0                 = new State
        val fetchT1                 = new State
        val execute                 = new State

        def fetch(src: UInt, reg: UInt, a: State, b: State, incIP: Boolean = true) = a.whenIsActive {
            cmd.addr                := src
            cmd.valid               := True
            when(cmd.fire) {
                cmd.valid           := False
                reg                 := rsp.data
                if(incIP) ip        := ip + 1
                goto(b)
            }
        }

        fetch(ip, a, fetchA, fetchB)
        fetch(ip, b, fetchB, fetchC)
        fetch(ip, c, fetchC, fetchT0)
        fetch(a, t0, fetchT0, fetchT1, false)
        fetch(b, t1, fetchT1, execute, false)

        execute.whenIsActive {
            cmd.addr                := b
            cmd.data                := sub
            cmd.write               := True
            cmd.valid               := True
            when(cmd.fire) {
                cmd.valid           := False
                when(br)(ip := c)
                goto(fetchA)
            }
        }
    }
}