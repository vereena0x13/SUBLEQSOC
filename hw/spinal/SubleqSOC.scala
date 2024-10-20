import scala.math

import scala.collection.mutable.ArrayBuffer
import spinal.core._


case class SubleqSOC(initial_memory: Option[Array[Short]]) extends Component {
    val io = new Bundle {
        val uart_wdata = out(Bits(8 bits))
        val uart_rdata = in(Bits(8 bits))
	    val uart_txe   = in(Bool())
	    val uart_rxf   = in(Bool())
	    val uart_wr    = out(Bool())
	    val uart_rd    = out(Bool())
    }
    import io._


    val cfg = SubleqConfig(
        width = 16
    )
    val subleq = Subleq(cfg)

    val sb_addr  = subleq.io.bus.addr
    val sb_wdata = subleq.io.bus.wdata
    val sb_write = subleq.io.bus.write


    val ramSize = math.pow(2, cfg.width).toInt
    val mem     = Mem(SInt(cfg.width bits), ramSize)
    if(initial_memory.isDefined) {
        val arr = new ArrayBuffer[SInt]
        arr ++= initial_memory.get.map(x => S(x))
        while(arr.length < ramSize) arr += S(0)
        mem.init(arr)
    }


    val b_uart_wdata = Reg(Bits(8 bits)) init(0)
    val b_uart_wr    = Reg(Bool()) init(False)
    //val b_uart_rd    = Reg(Bool()) init(False)

    uart_wdata  := b_uart_wdata
    uart_wr     := b_uart_wr 
    //uart_rd     := b_uart_rd
    uart_rd     := False


    when(sb_addr < 0) {
        when(sb_write) {
            b_uart_wdata := sb_wdata.trim(8).asBits
            subleq.io.bus.rdata := 0

            when(!uart_txe & !b_uart_wr) {
                subleq.io.bus.ready := True
                b_uart_wr := True
            } elsewhen(b_uart_wr) {
                subleq.io.bus.ready := True
                b_uart_wr := False
            } otherwise {
                subleq.io.bus.ready := False
            }
        } otherwise {
            // TODO
            subleq.io.bus.ready := True
            subleq.io.bus.rdata := 0
        }     
    } otherwise {
        subleq.io.bus.ready := True

        mem.write(
            address = sb_addr.asUInt,
            data    = sb_wdata,
            enable  = sb_write
        )

        subleq.io.bus.rdata := mem.readAsync(
            address = sb_addr.asUInt
        )
    }
}