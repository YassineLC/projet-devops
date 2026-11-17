import { useState } from 'react'
import { Toaster } from '@/components/ui/sonner'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Catalogue } from '@/pages/Catalogue'
import { MesEmprunts } from '@/pages/MesEmprunts'

function lireNom() {
  try {
    return localStorage.getItem('emprunteur') ?? ''
  } catch {
    return ''
  }
}

export default function App() {
  const [nom, setNomEtat] = useState(lireNom)

  const setNom = (n: string) => {
    setNomEtat(n)
    try {
      localStorage.setItem('emprunteur', n)
    } catch {
      /* stockage indisponible : le nom reste en mémoire */
    }
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-10 sm:px-8">
      <Tabs defaultValue="catalogue">
        <header className="mb-10 flex flex-wrap items-end justify-between gap-6 border-b border-border pb-6">
          <div>
            <h1 className="font-heading text-4xl font-semibold tracking-tight sm:text-5xl">Bibliothèque</h1>
            <p className="mt-2 text-muted-foreground">Choisissez un livre, il est à vous pour deux semaines.</p>
          </div>
          <TabsList>
            <TabsTrigger value="catalogue">Catalogue</TabsTrigger>
            <TabsTrigger value="emprunts">Mes emprunts</TabsTrigger>
          </TabsList>
        </header>
        <TabsContent value="catalogue">
          <Catalogue nom={nom} onNom={setNom} />
        </TabsContent>
        <TabsContent value="emprunts">
          <MesEmprunts nom={nom} onNom={setNom} />
        </TabsContent>
      </Tabs>
      <Toaster position="bottom-right" />
    </div>
  )
}
