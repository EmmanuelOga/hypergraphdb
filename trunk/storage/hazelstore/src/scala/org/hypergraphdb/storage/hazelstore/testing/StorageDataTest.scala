package org.hypergraphdb.storage.hazelstore.testing

import org.hypergraphdb.storage.HGStoreImplementation
import org.hypergraphdb.{HGConfiguration, HGPersistentHandle}
import org.hypergraphdb.storage.hazelstore.testing.TestCommons._
import scala.util.Try

/**
 * User: Ingvar Bogdahn
 * Date: 21.04.13
 */
class StorageDataTest ( val tested:HGStoreImplementation,
                        val testData:Seq[(HGPersistentHandle,Array[Byte])],
                        val async:Boolean,
                        override val keyPred :Boolean = random.nextBoolean(),
                        override val valPred :Boolean= random.nextBoolean())  extends HazelTest {

  type Tested = HGStoreImplementation
  type Data = (HGPersistentHandle,Array[Byte])

  setupStorImp(tested)

  val removeData = testData.filter(i => keyPred).map{ case (handle, handleArray) => (handle, handleArray.filter( j => valPred)) }

  val mutations:Seq[(String, Seq[Data], Seq[Data] => Unit)] = Seq(
    ("def store(handle: HGPersistentHandle, data: Array[Byte]): HGPersistentHandle", testData, (data:Seq[Data]) => data.foreach{case (handle, barray) => tested.store(handle, barray)}),
    ("def removeData(handle: HGPersistentHandle", removeData, (data:Seq[Data]) => data.foreach{case (handle, barray) => tested.removeData(handle)})
  )


  val validations:Seq[(String, (Tested,Seq[Data]) => Try[Boolean])]=
    Seq (
          ("def getData(handle: HGPersistentHandle): Array[Byte]", (store:Tested,a:Seq[Data]) => Try{
            val all  = a.forall{case (handle, barray) => repeatUntil(store.getData, handle)(arraysEqual(_,barray))._3}
            assert(all)
            log("asserted getData")
            all
          }),
          ("def containsData(handle: HGPersistentHandle): Boolean,",(store:Tested,a:Seq[Data]) => Try{
            val all  = a.forall{case (handle, _) => repeatUntil(store.containsData, handle)(_ == true)._3}
            assert(all)
            log("asserted containsData")
            all
          })
        )

}
