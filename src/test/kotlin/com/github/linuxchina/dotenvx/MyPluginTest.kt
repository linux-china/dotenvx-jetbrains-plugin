package com.github.linuxchina.dotenvx

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.psi.xml.XmlFile
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.PsiErrorElementUtil

@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class MyPluginTest : BasePlatformTestCase() {

    fun testXMLFile() {
        val psiFile = myFixture.configureByText(XmlFileType.INSTANCE, "<foo>bar</foo>")
        val xmlFile = assertInstanceOf(psiFile, XmlFile::class.java)

        assertFalse(PsiErrorElementUtil.hasErrors(project, xmlFile.virtualFile))

        assertNotNull(xmlFile.rootTag)

        xmlFile.rootTag?.let {
            assertEquals("foo", it.name)
            assertEquals("bar", it.value.text)
        }
    }

    fun testRename() {
        myFixture.testRename("foo.xml", "foo_after.xml", "a2")
    }

    fun testFindPublicKeyIgnoresMalformedDeclaration() {
        val psiFile = myFixture.configureByText(
            PlainTextFileType.INSTANCE,
            "# DOTENV_PUBLIC_KEY must be configured"
        )

        assertNull(DotenvxEncryptor.findPublicKey(psiFile))
    }

    fun testFindPublicKeyContinuesAfterMalformedDeclaration() {
        val psiFile = myFixture.configureByText(
            PlainTextFileType.INSTANCE,
            """
                # DOTENV_PUBLIC_KEY must be configured
                DOTENV_PUBLIC_KEY='0123456789abcdef'
            """.trimIndent()
        )

        assertEquals("0123456789abcdef", DotenvxEncryptor.findPublicKey(psiFile))
    }


    override fun getTestDataPath() = "src/test/testData/rename"
}
